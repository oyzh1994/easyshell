package cn.oyzh.easyshell.llm;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.store.llm.LLMMcpServerStore;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.resourcesastools.DefaultMcpResourcesAsToolsPresenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * 一次 LLM 请求使用的 MCP 客户端集合。
 *
 * <p>每个 Server 使用独立客户端；单个 Server 连接失败不会阻止其他 Server 或内置工具继续工作。</p>
 *
 * @author oyzh
 * @since 2026-11-20
 */
public final class LLMMcpToolSession implements AutoCloseable {

    static final String RESOURCE_LIST_TOOL = "mcp_resources_list";

    static final String RESOURCE_GET_TOOL = "mcp_resources_get";

    private static final int MAX_TOOL_NAME_LENGTH = 64;

    private static final int MAX_INSTRUCTIONS_LENGTH = 6000;

    private final List<McpClient> clients;

    private final List<ServerState> servers;

    private final McpToolProvider toolProvider;

    private final Set<String> toolNames;

    private final Map<String, String> toolSources;

    private final List<String> errors;

    private final String systemMessage;

    private boolean closed;

    private LLMMcpToolSession(List<ServerState> servers,
                              List<String> errors,
                              McpToolProvider toolProvider,
                              Set<String> toolNames,
                              Map<String, String> toolSources,
                              String systemMessage) {
        this.servers = List.copyOf(servers);
        this.errors = List.copyOf(errors);
        this.toolProvider = toolProvider;
        this.toolNames = Set.copyOf(toolNames);
        this.toolSources = Map.copyOf(toolSources);
        this.systemMessage = systemMessage;
        this.clients = servers.stream().map(ServerState::client).toList();
    }

    public static LLMMcpToolSession connectEnabled() {
        try {
            return connect(LLMMcpServerStore.INSTANCE.selectEnabled());
        } catch (Exception ex) {
            JulLog.warn("load mcp server configuration failed", ex);
            return new LLMMcpToolSession(List.of(),
                    List.of("configuration: " + errorMessage(ex)),
                    null, Set.of(), Map.of(), "");
        }
    }

    public static LLMMcpToolSession connect(Collection<LLMMcpServer> configurations) {
        List<ServerState> servers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        if (configurations != null) {
            for (LLMMcpServer configuration : configurations) {
                if (configuration == null || !configuration.isEnabled()) {
                    continue;
                }
                McpClient client = null;
                try {
                    client = LLMMcpClientFactory.createClient(configuration);
                    List<ToolSpecification> tools = client.listTools();
                    servers.add(new ServerState(configuration, client, tools,
                            LLMMcpConfigParser.parseToolNames(configuration.getToolNamesText())));
                } catch (Exception ex) {
                    closeQuietly(client);
                    String message = serverName(configuration) + ": " + errorMessage(ex);
                    errors.add(message);
                    JulLog.warn("connect mcp server failed: " + message, ex);
                }
            }
        }
        if (servers.isEmpty()) {
            return new LLMMcpToolSession(List.of(), errors, null, Set.of(), Map.of(), "");
        }

        Map<String, ServerState> serverByKey = new LinkedHashMap<>();
        Map<String, String> aliases = new LinkedHashMap<>();
        Set<String> toolNames = new LinkedHashSet<>();
        Map<String, String> toolSources = new LinkedHashMap<>();
        for (ServerState server : servers) {
            String key = server.client().key();
            serverByKey.put(key, server);
            aliases.put(key, toolPrefix(server.configuration(), key));
            for (ToolSpecification specification : server.tools()) {
                if (!server.accepts(specification.name())) {
                    continue;
                }
                String mappedName = mapToolName(aliases.get(key), specification.name());
                toolNames.add(mappedName);
                toolSources.put(mappedName, "MCP:" + serverName(server.configuration())
                        + "/" + specification.name());
            }
        }
        toolNames.add(RESOURCE_LIST_TOOL);
        toolNames.add(RESOURCE_GET_TOOL);
        toolSources.put(RESOURCE_LIST_TOOL, "MCP:resources");
        toolSources.put(RESOURCE_GET_TOOL, "MCP:resources");

        BiPredicate<McpClient, ToolSpecification> filter = (client, specification) -> {
            ServerState server = serverByKey.get(client.key());
            return server != null && server.accepts(specification.name());
        };
        DefaultMcpResourcesAsToolsPresenter resourcesPresenter =
                DefaultMcpResourcesAsToolsPresenter.builder()
                        .nameOfListResourcesTool(RESOURCE_LIST_TOOL)
                        .nameOfGetResourceTool(RESOURCE_GET_TOOL)
                        .descriptionOfListResourcesTool(
                                "List resources exposed by the configured MCP servers.")
                        .descriptionOfGetResourceTool(
                                "Read an MCP resource by server key and resource URI.")
                        .build();
        McpToolProvider provider = McpToolProvider.builder()
                .mcpClients(servers.stream().map(ServerState::client).toList())
                .filter(filter)
                .toolNameMapper((client, specification) ->
                        mapToolName(aliases.get(client.key()), specification.name()))
                .resourcesAsToolsPresenter(resourcesPresenter)
                .failIfOneServerFails(false)
                .build();
        String systemMessage = buildSystemMessage(servers, aliases, errors);
        return new LLMMcpToolSession(servers, errors, provider, toolNames, toolSources, systemMessage);
    }

    private static String buildSystemMessage(List<ServerState> servers,
                                             Map<String, String> aliases,
                                             List<String> errors) {
        StringBuilder message = new StringBuilder()
                .append("## Model Context Protocol (MCP)\n")
                .append("MCP tools are available through the current request. ")
                .append("Use only the exact tool names listed below. ")
                .append("The server key is required when reading MCP resources.\n");
        for (ServerState server : servers) {
            String key = server.client().key();
            String prefix = aliases.get(key);
            message.append("\n- Server: ").append(serverName(server.configuration()))
                    .append(" | key: ").append(key)
                    .append(" | transport: ").append(server.configuration().getTransportName())
                    .append('\n');
            List<String> mapped = server.tools().stream()
                    .filter(specification -> server.accepts(specification.name()))
                    .map(specification -> mapToolName(prefix, specification.name()))
                    .toList();
            if (mapped.isEmpty()) {
                message.append("  Tools: none\n");
            } else {
                message.append("  Tools: ").append(String.join(", ", mapped)).append('\n');
            }
            try {
                String instructions = server.client().instructions();
                if (instructions != null && !instructions.isBlank()) {
                    message.append("  Server instructions:\n")
                            .append(indent(limit(instructions.trim(), MAX_INSTRUCTIONS_LENGTH)))
                            .append('\n');
                }
            } catch (Exception ex) {
                JulLog.warn("read mcp server instructions failed: " + key, ex);
            }
        }
        message.append("\nResource helpers: ").append(RESOURCE_LIST_TOOL)
                .append(", ").append(RESOURCE_GET_TOOL).append('\n');
        if (!errors.isEmpty()) {
            message.append("\nUnavailable MCP servers (do not call tools from these):\n");
            for (String error : errors) {
                message.append("- ").append(error).append('\n');
            }
        }
        return message.toString();
    }

    public boolean hasClients() {
        return !this.clients.isEmpty();
    }

    public McpToolProvider getToolProvider() {
        return toolProvider;
    }

    public Set<String> getToolNames() {
        return toolNames;
    }

    public Map<String, String> getToolSources() {
        return toolSources;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        for (McpClient client : this.clients) {
            closeQuietly(client);
        }
    }

    static String mapToolName(String prefix, String originalName) {
        String tool = sanitize(originalName, true);
        if (tool.isEmpty()) {
            tool = "tool";
        }
        String name = prefix + "__" + tool;
        if (name.length() <= MAX_TOOL_NAME_LENGTH) {
            return name;
        }
        return name.substring(0, MAX_TOOL_NAME_LENGTH - 8) + "_" + shortHash(originalName);
    }

    static String toolPrefix(LLMMcpServer server, String key) {
        String name = sanitize(serverName(server), false);
        if (name.isEmpty()) {
            name = "server";
        }
        if (name.length() > 18) {
            name = name.substring(0, 18);
        }
        return "mcp_" + name + "_" + shortHash(key);
    }

    private static String sanitize(String value, boolean keepDash) {
        if (value == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        boolean underscore = false;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            boolean valid = character >= 'a' && character <= 'z'
                    || character >= 'A' && character <= 'Z'
                    || character >= '0' && character <= '9'
                    || character == '_'
                    || keepDash && character == '-';
            if (valid) {
                result.append(Character.toLowerCase(character));
                underscore = false;
            } else if (!underscore) {
                result.append('_');
                underscore = true;
            }
        }
        while (!result.isEmpty() && result.charAt(result.length() - 1) == '_') {
            result.deleteCharAt(result.length() - 1);
        }
        while (!result.isEmpty() && result.charAt(0) == '_') {
            result.deleteCharAt(0);
        }
        return result.toString();
    }

    private static String shortHash(String value) {
        return Integer.toUnsignedString(String.valueOf(value).hashCode(), 36);
    }

    private static String serverName(LLMMcpServer server) {
        if (server == null) {
            return "MCP server";
        }
        if (server.getName() != null && !server.getName().isBlank()) {
            return server.getName().trim();
        }
        if (server.getEndpointText() != null && !server.getEndpointText().isBlank()) {
            return server.getEndpointText();
        }
        return "MCP server";
    }

    private static String errorMessage(Exception ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank()
                ? current.getClass().getSimpleName()
                : message;
    }

    private static String limit(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "\n...[truncated]";
    }

    private static String indent(String value) {
        return value.lines().map(line -> "    " + line).reduce((first, second) -> first + "\n" + second)
                .orElse("    ");
    }

    private static void closeQuietly(McpClient client) {
        if (client == null) {
            return;
        }
        try {
            client.close();
        } catch (Exception ex) {
            JulLog.warn("close mcp client failed", ex);
        }
    }

    private record ServerState(LLMMcpServer configuration,
                               McpClient client,
                               List<ToolSpecification> tools,
                               Set<String> allowedTools) {

        private boolean accepts(String toolName) {
            return this.allowedTools.isEmpty() || this.allowedTools.contains(toolName);
        }
    }
}
