package cn.oyzh.easyshell.terminal.redis2;

import redis.clients.jedis.util.SafeEncoder;

import java.util.Collection;
import java.util.Map;

/**
 * 将 Jedis 的 RESP 返回值按 redis-cli 风格输出。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public final class RedisResponseFormatter {

    private RedisResponseFormatter() {
    }

    /**
     * 将 Redis 返回值格式化为终端文本。
     *
     * @param value       Redis 返回值
     * @param lineEnding  终端换行符
     * @return 格式化文本
     */
    public static String format(Object value, String lineEnding) {
        String ending = lineEnding == null || lineEnding.isEmpty() ? "\n" : lineEnding;
        return formatValue(value, ending, 0, true);
    }

    /**
     * 递归格式化任意 RESP 返回值。
     */
    private static String formatValue(Object value, String ending, int indent, boolean topLevel) {
        if (value == null) {
            return "(nil)";
        }
        if (value instanceof byte[] bytes) {
            return quote(SafeEncoder.encode(bytes));
        }
        if (value instanceof Map<?, ?> map) {
            return formatMap(map, ending, indent);
        }
        if (value instanceof Collection<?> collection) {
            return formatCollection(collection, ending, indent, topLevel);
        }
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return "(integer) " + value;
        }
        if (value instanceof Double || value instanceof Float) {
            return "(double) " + value;
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        return value.toString();
    }

    /**
     * 按 redis-cli 列表样式格式化集合。
     */
    private static String formatCollection(Collection<?> values, String ending, int indent, boolean topLevel) {
        if (values.isEmpty()) {
            return topLevel ? "(empty list or set)" : "(empty list or set)";
        }
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (Object value : values) {
            if (index > 1) {
                builder.append(ending).append(" ".repeat(indent));
            }
            builder.append(index++).append(") ");
            builder.append(formatValue(value, ending, indent + 3, false));
        }
        return builder.toString();
    }

    /**
     * 按字段和值样式格式化映射。
     */
    private static String formatMap(Map<?, ?> map, String ending, int indent) {
        if (map.isEmpty()) {
            return "(empty map)";
        }
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (index > 1) {
                builder.append(ending).append(" ".repeat(indent));
            }
            builder.append(index++).append("# ");
            builder.append(formatValue(entry.getKey(), ending, indent + 3, false));
            builder.append(" => ");
            builder.append(formatValue(entry.getValue(), ending, indent + 3, false));
        }
        return builder.toString();
    }

    /**
     * 对字符串内容进行转义并添加双引号。
     */
    private static String quote(String value) {
        StringBuilder builder = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            switch (current) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> builder.append(current);
            }
        }
        return builder.append('"').toString();
    }
}
