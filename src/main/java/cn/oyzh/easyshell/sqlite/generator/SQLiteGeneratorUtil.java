package cn.oyzh.easyshell.sqlite.generator;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.Map;

/**
 * SQLite SQL 生成基础工具。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public final class SQLiteGeneratorUtil {

    private SQLiteGeneratorUtil() {
    }

    /**
     * 转换对象名称
     *
     * @param name 对象名称
     * @return 转换后的对象名称
     */
    public static String quote(String name) {
        return DBUtil.wrap(name, DBDialect.SQLITE);
    }

    /**
     * 还原对象名称
     *
     * @param name 对象名称
     * @return 还原后的对象名称
     */
    public static String unquote(String name) {
        if (name == null || name.length() < 2) {
            return name;
        }
        char first = name.charAt(0);
        char last = name.charAt(name.length() - 1);
        if (first == '"' && last == '"') {
            return name.substring(1, name.length() - 1).replace("\"\"", "\"");
        }
        if (first == '`' && last == '`') {
            return name.substring(1, name.length() - 1).replace("``", "`");
        }
        if (first == '[' && last == ']') {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }

    /**
     * 转换SQL字面量
     *
     * @param value 数据值
     * @return SQL字面量
     */
    public static String literal(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof byte[] bytes) {
            StringBuilder builder = new StringBuilder("X'");
            for (byte item : bytes) {
                builder.append(String.format("%02X", item));
            }
            return builder.append("'").toString();
        }
        return String.valueOf(DBUtil.wrapData(value, DBDialect.SQLITE));
    }

    /**
     * 移除SQL末尾分号
     *
     * @param sql SQL语句
     * @return SQL语句
     */
    public static String stripSemicolon(String sql) {
        return sql.trim().replaceAll(";\\s*$", "");
    }

    /**
     * 查找SQL关键字位置
     *
     * @param sql       SQL语句
     * @param keyword   SQL关键字
     * @param fromIndex 开始位置
     * @return 关键字位置，不存在时返回-1
     */
    public static int keywordIndex(String sql, String keyword, int fromIndex) {
        char quote = 0;
        for (int index = Math.max(0, fromIndex); index <= sql.length() - keyword.length(); index++) {
            char value = sql.charAt(index);
            if (quote != 0) {
                if (value == quote) {
                    if (quote != ']' && index + 1 < sql.length() && sql.charAt(index + 1) == quote) {
                        index++;
                    } else {
                        quote = 0;
                    }
                } else if (quote == ']' && value == ']') {
                    quote = 0;
                }
                continue;
            }
            if (value == '\'' || value == '"' || value == '`') {
                quote = value;
            } else if (value == '[') {
                quote = ']';
            } else if (sql.regionMatches(true, index, keyword, 0, keyword.length())
                    && (index == 0 || !Character.isLetterOrDigit(sql.charAt(index - 1)))
                    && (index + keyword.length() == sql.length()
                    || !Character.isLetterOrDigit(sql.charAt(index + keyword.length())))) {
                return index;
            }
        }
        return -1;
    }

    /**
     * 替换SQL表达式中的字段引用，保留字符串、注释和函数名称。
     *
     * @param sql          SQL表达式或约束
     * @param replacements 字段名称映射，键为原名称，值为新名称
     * @return 替换后的SQL
     */
    public static String replaceColumnReferences(String sql, Map<String, String> replacements) {
        if (StringUtil.isBlank(sql) || replacements == null || replacements.isEmpty()) {
            return sql;
        }
        StringBuilder result = new StringBuilder(sql.length());
        boolean afterConstraint = false;
        int index = 0;
        while (index < sql.length()) {
            char value = sql.charAt(index);
            if (value == '-' && index + 1 < sql.length() && sql.charAt(index + 1) == '-') {
                int end = sql.indexOf('\n', index + 2);
                end = end < 0 ? sql.length() : end + 1;
                result.append(sql, index, end);
                index = end;
                continue;
            }
            if (value == '/' && index + 1 < sql.length() && sql.charAt(index + 1) == '*') {
                int end = sql.indexOf("*/", index + 2);
                end = end < 0 ? sql.length() : end + 2;
                result.append(sql, index, end);
                index = end;
                continue;
            }
            if (value == '\'') {
                int end = quotedEnd(sql, index, '\'', '\'');
                result.append(sql, index, end);
                index = end;
                continue;
            }
            if (value == '"' || value == '`' || value == '[') {
                char closing = value == '[' ? ']' : value;
                int end = quotedEnd(sql, index, value, closing);
                String quotedName = sql.substring(index, end);
                String name = SQLiteGeneratorUtil.unquote(quotedName);
                if (afterConstraint) {
                    result.append(quotedName);
                    afterConstraint = false;
                } else {
                    String replacement = replacementName(name, replacements);
                    result.append(replacement == null ? quotedName : quote(replacement));
                }
                index = end;
                continue;
            }
            if (isIdentifierStart(value)) {
                int end = index + 1;
                while (end < sql.length() && isIdentifierPart(sql.charAt(end))) {
                    end++;
                }
                String name = sql.substring(index, end);
                int previous = previousSignificant(sql, index);
                int next = nextSignificant(sql, end);
                boolean functionCall = next >= 0 && sql.charAt(next) == '(';
                boolean qualifier = next >= 0 && sql.charAt(next) == '.';
                boolean qualifiedColumn = previous >= 0 && sql.charAt(previous) == '.';
                if (afterConstraint) {
                    result.append(name);
                    afterConstraint = false;
                } else if (functionCall || (qualifier && !qualifiedColumn)) {
                    result.append(name);
                } else {
                    String replacement = replacementName(name, replacements);
                    result.append(replacement == null ? name : quote(replacement));
                }
                if ("CONSTRAINT".equalsIgnoreCase(name)) {
                    afterConstraint = true;
                }
                index = end;
                continue;
            }
            result.append(value);
            index++;
        }
        return result.toString();
    }

    private static String replacementName(String name, Map<String, String> replacements) {
        for (Map.Entry<String, String> item : replacements.entrySet()) {
            if (item.getKey() != null && item.getKey().equalsIgnoreCase(name)) {
                return StringUtil.blankToDefault(item.getValue(), name);
            }
        }
        return null;
    }

    private static int quotedEnd(String sql, int start, char opening, char closing) {
        int index = start + 1;
        while (index < sql.length()) {
            char value = sql.charAt(index);
            if (value == closing) {
                if (opening != '[' && index + 1 < sql.length() && sql.charAt(index + 1) == closing) {
                    index += 2;
                    continue;
                }
                return index + 1;
            }
            index++;
        }
        return sql.length();
    }

    private static int previousSignificant(String sql, int start) {
        for (int index = start - 1; index >= 0; index--) {
            if (!Character.isWhitespace(sql.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static int nextSignificant(String sql, int start) {
        for (int index = start; index < sql.length(); index++) {
            if (!Character.isWhitespace(sql.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static boolean isIdentifierStart(char value) {
        return Character.isLetter(value) || value == '_' || Character.getType(value) == Character.CURRENCY_SYMBOL;
    }

    private static boolean isIdentifierPart(char value) {
        return Character.isLetterOrDigit(value) || value == '_' || value == '$'
                || Character.getType(value) == Character.CURRENCY_SYMBOL;
    }

    /**
     * 校验对象名称
     *
     * @param name 对象名称
     */
    public static void requireName(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("SQLite object name is empty");
        }
    }
}
