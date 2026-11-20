package cn.oyzh.easyshell.llm.mcp;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 标准 MCP JSON 配置解析器。
 *
 * <p>兼容 Claude Desktop 等工具常见的 {@code mcpServers} 配置结构。</p>
 *
 * @author oyzh
 * @since 2026-11-20
 */
public final class LLMMcpJsonConfigParser {

    private LLMMcpJsonConfigParser() {
    }

    /**
     * 解析包含一个或多个 Server 的 MCP JSON 配置。
     *
     * @param json JSON 文本
     * @return Server 配置
     */
    public static List<LLMMcpServer> parse(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("MCP JSON configuration cannot be empty");
        }
        JSONObject root;
        try {
            root = JSON.parseObject(json);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid MCP JSON: " + ex.getMessage(), ex);
        }
        if (root == null) {
            throw new IllegalArgumentException("Invalid MCP JSON configuration");
        }
        Object container = root.get("mcpServers");
        if (container == null) {
            String name = value(root.get("name"));
            return List.of(parseServer(name == null || name.isBlank() ? "MCP Server" : name, root));
        }
        JSONObject servers = asObject(container, "mcpServers");
        if (servers.isEmpty()) {
            throw new IllegalArgumentException("mcpServers cannot be empty");
        }
        List<LLMMcpServer> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : servers.entrySet()) {
            result.add(parseServer(entry.getKey(), asObject(entry.getValue(), entry.getKey())));
        }
        return List.copyOf(result);
    }

    /**
     * 将命令和参数转换为现有配置模型使用的命令行文本。
     */
    public static String toCommandText(String command, Collection<?> arguments) {
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("command cannot be empty");
        }
        List<String> values = new ArrayList<>();
        values.add(command.trim());
        if (arguments != null) {
            for (Object argument : arguments) {
                values.add(argument == null ? "" : String.valueOf(argument));
            }
        }
        return values.stream().map(LLMMcpJsonConfigParser::quoteArgument)
                .reduce((first, second) -> first + " " + second)
                .orElseThrow();
    }

    private static LLMMcpServer parseServer(String name, JSONObject object) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("MCP server name cannot be empty");
        }
        LLMMcpServer server = new LLMMcpServer();
        server.setName(name.trim());
        server.setEnabled(readEnabled(object));
        server.setTimeoutSeconds(readTimeout(object));

        String command = value(object.get("command"));
        String url = value(object.get("url"));
        if (command != null && !command.isBlank()) {
            server.setTransport(LLMMcpTransport.STDIO);
            JSONArray args = asArray(object.get("args"), "args");
            server.setCommandText(toCommandText(command, args));
            server.setEnvironmentText(formatEnvironment(asObject(object.get("env"), "env")));
            String cwd = firstNonBlank(value(object.get("cwd")), value(object.get("workingDirectory")));
            server.setWorkingDirectory(cwd);
        } else if (url != null && !url.isBlank()) {
            LLMMcpTransport transport = parseTransport(object, url);
            server.setTransport(transport);
            server.setUrl(url.trim());
            server.setHeadersText(formatHeaders(asObject(object.get("headers"), "headers")));
        } else {
            throw new IllegalArgumentException(
                    "MCP server '" + name + "' must define either command or url");
        }

        Set<String> tools = readStringSet(firstNonNull(
                object.get("toolNames"), object.get("enabledTools"), object.get("tools")));
        if (!tools.isEmpty()) {
            server.setToolNamesText(String.join(System.lineSeparator(), tools));
        }
        LLMMcpClientFactory.validateConfiguration(server);
        return server;
    }

    private static LLMMcpTransport parseTransport(JSONObject object, String url) {
        String type = firstNonBlank(value(object.get("type")), value(object.get("transport")));
        if (type == null || type.isBlank()) {
            return url.startsWith("ws://") || url.startsWith("wss://")
                    ? LLMMcpTransport.WEBSOCKET
                    : LLMMcpTransport.STREAMABLE_HTTP;
        }
        String normalized = type.toLowerCase(Locale.ROOT)
                .replace("_", "")
                .replace("-", "");
        return switch (normalized) {
            case "http", "streamablehttp" -> LLMMcpTransport.STREAMABLE_HTTP;
            case "ws", "wss", "websocket" -> LLMMcpTransport.WEBSOCKET;
            case "stdio" -> throw new IllegalArgumentException(
                    "MCP server transport stdio requires command");
            case "sse" -> throw new IllegalArgumentException(
                    "SSE transport is not supported; use Streamable HTTP instead");
            default -> throw new IllegalArgumentException("Unsupported MCP transport: " + type);
        };
    }

    private static boolean readEnabled(JSONObject object) {
        Boolean disabled = booleanValue(object.get("disabled"));
        if (disabled != null) {
            return !disabled;
        }
        Boolean enabled = booleanValue(object.get("enabled"));
        return enabled == null || enabled;
    }

    private static int readTimeout(JSONObject object) {
        Object timeout = firstNonNull(object.get("timeoutSeconds"), object.get("timeout"));
        if (timeout == null) {
            return 60;
        }
        try {
            int value = Integer.parseInt(String.valueOf(timeout));
            return Math.max(1, Math.min(value, 3600));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid MCP timeout: " + timeout, ex);
        }
    }

    private static String formatEnvironment(JSONObject environment) {
        if (environment == null || environment.isEmpty()) {
            return null;
        }
        List<String> lines = new ArrayList<>();
        environment.forEach((key, value) ->
                lines.add(key + "=" + (value == null ? "" : value)));
        return String.join(System.lineSeparator(), lines);
    }

    private static String formatHeaders(JSONObject headers) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        List<String> lines = new ArrayList<>();
        headers.forEach((key, value) ->
                lines.add(key + ": " + (value == null ? "" : value)));
        return String.join(System.lineSeparator(), lines);
    }

    private static Set<String> readStringSet(Object value) {
        Set<String> result = new LinkedHashSet<>();
        if (value == null) {
            return result;
        }
        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                String text = value(item);
                if (text != null && !text.isBlank()) {
                    result.add(text.trim());
                }
            }
        } else {
            result.addAll(LLMMcpConfigParser.parseToolNames(String.valueOf(value)));
        }
        return result;
    }

    private static JSONObject asObject(Object value, String field) {
        if (value == null) {
            return null;
        }
        if (value instanceof JSONObject object) {
            return object;
        }
        if (value instanceof Map<?, ?> map) {
            JSONObject result = new JSONObject();
            map.forEach((key, item) -> result.put(String.valueOf(key), item));
            return result;
        }
        throw new IllegalArgumentException(field + " must be a JSON object");
    }

    private static JSONArray asArray(Object value, String field) {
        if (value == null) {
            return null;
        }
        if (value instanceof JSONArray array) {
            return array;
        }
        if (value instanceof Collection<?> collection) {
            return JSONArray.from(collection);
        }
        throw new IllegalArgumentException(field + " must be a JSON array");
    }

    private static Boolean booleanValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = String.valueOf(value).trim();
        if ("true".equalsIgnoreCase(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text)) {
            return false;
        }
        throw new IllegalArgumentException("Expected a boolean value: " + value);
    }

    private static String value(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first.trim() : second;
    }

    private static Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String quoteArgument(String value) {
        if (!value.isEmpty() && value.matches("[A-Za-z0-9_./:=@%+,~-]+")) {
            return value;
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
