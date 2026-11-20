package cn.oyzh.easyshell.llm.mcp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP 配置文本解析工具。
 *
 * @author oyzh
 * @since 2026-11-20
 */
public final class LLMMcpConfigParser {

    private LLMMcpConfigParser() {
    }

    /**
     * 解析启动命令，支持单引号、双引号和反斜杠转义。
     *
     * @param text 命令文本
     * @return 参数列表
     */
    public static List<String> parseCommandLine(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        char quote = 0;
        boolean tokenStarted = false;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (quote != 0) {
                if (character == quote) {
                    quote = 0;
                } else if (character == '\\' && index + 1 < text.length()) {
                    char next = text.charAt(index + 1);
                    if (next == quote || next == '\\') {
                        token.append(next);
                        index++;
                    } else {
                        token.append(character);
                    }
                } else {
                    token.append(character);
                }
                tokenStarted = true;
                continue;
            }
            if (character == '\'' || character == '"') {
                quote = character;
                tokenStarted = true;
            } else if (Character.isWhitespace(character)) {
                if (tokenStarted) {
                    result.add(token.toString());
                    token.setLength(0);
                    tokenStarted = false;
                }
            } else if (character == '\\' && index + 1 < text.length()) {
                char next = text.charAt(index + 1);
                if (Character.isWhitespace(next) || next == '\'' || next == '"' || next == '\\') {
                    token.append(next);
                    index++;
                } else {
                    token.append(character);
                }
                tokenStarted = true;
            } else {
                token.append(character);
                tokenStarted = true;
            }
        }
        if (quote != 0) {
            throw new IllegalArgumentException("Unterminated quote in command");
        }
        if (tokenStarted) {
            result.add(token.toString());
        }
        return List.copyOf(result);
    }

    public static Map<String, String> parseEnvironment(String text) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String line : lines(text)) {
            int separator = line.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            if (!key.isEmpty()) {
                result.put(key, line.substring(separator + 1).trim());
            }
        }
        return result;
    }

    public static Map<String, String> parseHeaders(String text) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String line : lines(text)) {
            int separator = line.indexOf(':');
            if (separator <= 0) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            if (!key.isEmpty()) {
                result.put(key, line.substring(separator + 1).trim());
            }
        }
        return result;
    }

    public static Set<String> parseToolNames(String text) {
        Set<String> result = new LinkedHashSet<>();
        if (text == null || text.isBlank()) {
            return result;
        }
        for (String value : text.split("[\\r\\n,]+")) {
            String name = value.trim();
            if (!name.isEmpty()) {
                result.add(name);
            }
        }
        return result;
    }

    /**
     * 展开 ${NAME} 变量，未定义的变量保持原样。
     */
    public static String expandVariables(String text, Map<String, String> localVariables) {
        if (text == null || text.indexOf('$') < 0) {
            return text;
        }
        StringBuilder result = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '$' && index + 2 < text.length() && text.charAt(index + 1) == '{') {
                int end = text.indexOf('}', index + 2);
                if (end > index + 2) {
                    String name = text.substring(index + 2, end);
                    String value = localVariables == null ? null : localVariables.get(name);
                    if (value == null) {
                        value = System.getenv(name);
                    }
                    if (value != null) {
                        result.append(value);
                        index = end;
                        continue;
                    }
                }
            }
            result.append(character);
        }
        return result.toString();
    }

    private static List<String> lines(String text) {
        List<String> result = new ArrayList<>();
        if (text == null) {
            return result;
        }
        for (String value : text.split("\\R")) {
            String line = value.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                result.add(line);
            }
        }
        return result;
    }
}
