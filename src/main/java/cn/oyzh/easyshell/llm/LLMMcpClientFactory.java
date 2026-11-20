package cn.oyzh.easyshell.llm;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.mcp.client.transport.websocket.WebSocketMcpTransport;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据持久化配置创建 MCP 客户端。
 *
 * @author oyzh
 * @since 2026-11-20
 */
public final class LLMMcpClientFactory {

    private static final String CLIENT_NAME = "EasyShell";

    private LLMMcpClientFactory() {
    }

    public static McpClient createClient(LLMMcpServer server) {
        if (server == null) {
            throw new IllegalArgumentException("MCP server is required");
        }
        validateConfiguration(server);
        McpTransport transport = createTransport(server);
        Duration timeout = Duration.ofSeconds(server.getTimeoutSeconds());
        try {
            return DefaultMcpClient.builder()
                    .transport(transport)
                    .key(clientKey(server))
                    .clientName(CLIENT_NAME)
                    .clientVersion(clientVersion())
                    .initializationTimeout(timeout)
                    .toolExecutionTimeout(timeout)
                    .resourcesTimeout(timeout)
                    .promptsTimeout(timeout)
                    .pingTimeout(timeout)
                    .cacheToolList(true)
                    .cacheResourceList(true)
                    .cachePromptList(true)
                    .build();
        } catch (RuntimeException ex) {
            closeQuietly(transport);
            throw ex;
        }
    }

    public static McpTransport createTransport(LLMMcpServer server) {
        validateConfiguration(server);
        return switch (server.getTransport()) {
            case STDIO -> createStdioTransport(server);
            case STREAMABLE_HTTP -> createHttpTransport(server);
            case WEBSOCKET -> createWebSocketTransport(server);
        };
    }

    public static void validateConfiguration(LLMMcpServer server) {
        if (server == null) {
            throw new IllegalArgumentException("MCP server is required");
        }
        switch (server.getTransport()) {
            case STDIO -> {
                if (LLMMcpConfigParser.parseCommandLine(server.getCommandText()).isEmpty()) {
                    throw new IllegalArgumentException("MCP stdio command cannot be empty");
                }
                if (server.getWorkingDirectory() != null && !server.getWorkingDirectory().isBlank()) {
                    Path.of(server.getWorkingDirectory().trim());
                }
                LLMMcpConfigParser.parseEnvironment(server.getEnvironmentText());
            }
            case STREAMABLE_HTTP -> {
                requireUri(resolveUrl(server), "http", "https");
                LLMMcpConfigParser.parseHeaders(server.getHeadersText());
            }
            case WEBSOCKET -> {
                requireUri(resolveUrl(server), "ws", "wss");
                LLMMcpConfigParser.parseHeaders(server.getHeadersText());
            }
        }
    }

    private static McpTransport createStdioTransport(LLMMcpServer server) {
        Map<String, String> environment = LLMMcpConfigParser.parseEnvironment(server.getEnvironmentText());
        List<String> command = LLMMcpConfigParser.parseCommandLine(server.getCommandText()).stream()
                .map(value -> LLMMcpConfigParser.expandVariables(value, environment))
                .toList();
        if (command.isEmpty()) {
            throw new IllegalArgumentException("MCP stdio command cannot be empty");
        }
        StdioMcpTransport.Builder builder = StdioMcpTransport.builder()
                .command(command)
                .logEvents(false);
        if (!environment.isEmpty()) {
            Map<String, String> expanded = new LinkedHashMap<>();
            environment.forEach((key, value) ->
                    expanded.put(key, LLMMcpConfigParser.expandVariables(value, environment)));
            builder.environment(expanded);
        }
        if (server.getWorkingDirectory() != null && !server.getWorkingDirectory().isBlank()) {
            builder.workingDirectory(Path.of(
                    LLMMcpConfigParser.expandVariables(server.getWorkingDirectory().trim(), environment)));
        }
        return builder.build();
    }

    private static McpTransport createHttpTransport(LLMMcpServer server) {
        URI uri = requireUri(resolveUrl(server), "http", "https");
        return StreamableHttpMcpTransport.builder()
                .url(uri.toString())
                .customHeaders(expandedHeaders(server))
                .timeout(Duration.ofSeconds(server.getTimeoutSeconds()))
                .build();
    }

    private static McpTransport createWebSocketTransport(LLMMcpServer server) {
        URI uri = requireUri(resolveUrl(server), "ws", "wss");
        return WebSocketMcpTransport.builder()
                .url(uri.toString())
                .headersSupplier(() -> expandedHeaders(server))
                .timeout(Duration.ofSeconds(server.getTimeoutSeconds()))
                .build();
    }

    private static Map<String, String> expandedHeaders(LLMMcpServer server) {
        Map<String, String> headers = LLMMcpConfigParser.parseHeaders(server.getHeadersText());
        if (headers.isEmpty()) {
            return headers;
        }
        Map<String, String> result = new LinkedHashMap<>();
        headers.forEach((key, value) ->
                result.put(key, LLMMcpConfigParser.expandVariables(value, null)));
        return result;
    }

    private static String resolveUrl(LLMMcpServer server) {
        return LLMMcpConfigParser.expandVariables(server.getUrl(), null);
    }

    private static URI requireUri(String value, String... schemes) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("MCP server URL cannot be empty");
        }
        URI uri;
        try {
            uri = URI.create(value.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid MCP server URL: " + value, ex);
        }
        if (uri.getScheme() == null) {
            throw new IllegalArgumentException("MCP server URL must include a scheme");
        }
        for (String scheme : schemes) {
            if (scheme.equalsIgnoreCase(uri.getScheme())) {
                return uri;
            }
        }
        throw new IllegalArgumentException("Unsupported MCP URL scheme: " + uri.getScheme());
    }

    static String clientKey(LLMMcpServer server) {
        if (server.getUid() != null && !server.getUid().isBlank()) {
            return server.getUid();
        }
        if (server.getName() != null && !server.getName().isBlank()) {
            return server.getName().trim();
        }
        return "mcp-server";
    }

    private static String clientVersion() {
        String version = LLMMcpClientFactory.class.getPackage().getImplementationVersion();
        return version == null || version.isBlank() ? "dev" : version;
    }

    private static void closeQuietly(McpTransport transport) {
        try {
            transport.close();
        } catch (Exception ignored) {
        }
    }
}
