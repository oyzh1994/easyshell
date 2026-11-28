package cn.oyzh.easyshell.data.h2.util;

import cn.oyzh.common.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * H2 SQL 文件 schema 重写工具。
 *
 * <p>EasyShell 转储会生成完整的 schema 限定名。运行 SQL 文件到其它 schema 时，
 * 需要先重写这些限定名，否则显式限定名会覆盖执行会话的目标 schema。</p>
 *
 * @since 2026-11-28
 */
public final class H2SqlSchemaRewriter {

    private static final Pattern SOURCE_MARKER = Pattern.compile(
            "(?im)^\\s*--\\s*EasyShell H2 source schema:\\s*(?:\"((?:[^\"]|\"\")*)\"|([^\\s]+))\\s*$");

    private static final Pattern CREATE_SCHEMA = Pattern.compile(
            "(?is)\\bCREATE\\s+SCHEMA\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?"
                    + "(?:\"((?:[^\"]|\"\")*)\"|([A-Za-z_][\\w$]*))");

    private static final Pattern QUALIFIED_SCHEMA = Pattern.compile(
            "(?s)\"((?:[^\"]|\"\")*)\"\\s*\\.\\s*"
                    + "(?:\"(?:[^\"]|\"\")*\"|[A-Za-z_][\\w$]*)");

    private static final Pattern SCHEMA_STATEMENT = Pattern.compile(
            "(?is).*\\b(?:CREATE\\s+SCHEMA(?:\\s+IF\\s+NOT\\s+EXISTS)?"
                    + "|ALTER\\s+SCHEMA|DROP\\s+SCHEMA(?:\\s+IF\\s+EXISTS)?"
                    + "|SET\\s+SCHEMA)\\s*$");

    private H2SqlSchemaRewriter() {
    }

    /**
     * 从转储内容中解析源 schema。
     *
     * @param content SQL 文件内容
     * @return 源 schema，无法解析时返回 null
     */
    public static String sourceSchema(String content) {
        if (StringUtil.isBlank(content)) {
            return null;
        }
        Matcher marker = SOURCE_MARKER.matcher(content);
        if (marker.find()) {
            return unquote(StringUtil.blankToDefault(
                    marker.group(1), marker.group(2)));
        }
        Matcher create = CREATE_SCHEMA.matcher(content);
        if (create.find()) {
            return unquote(StringUtil.blankToDefault(
                    create.group(1), create.group(2)));
        }
        Matcher qualified = QUALIFIED_SCHEMA.matcher(content);
        return qualified.find() ? unquote(qualified.group(1)) : null;
    }

    /**
     * 将单条 SQL 中的源 schema 限定名重写为目标 schema。
     *
     * @param sql           SQL
     * @param sourceSchema  源 schema
     * @param targetSchema  目标 schema
     * @return 重写后的 SQL
     */
    public static String rewriteStatement(String sql, String sourceSchema,
                                          String targetSchema) {
        if (StringUtil.isBlank(sql) || StringUtil.isBlank(sourceSchema)
                || StringUtil.isBlank(targetSchema)
                || sourceSchema.equalsIgnoreCase(targetSchema)) {
            return sql;
        }
        String source = unquote(sourceSchema);
        String target = unquote(targetSchema);
        String targetToken = quote(target);
        StringBuilder result = new StringBuilder(sql.length());
        int index = 0;
        while (index < sql.length()) {
            char ch = sql.charAt(index);
            if (ch == '\'') {
                int end = skipQuoted(sql, index, '\'');
                result.append(sql, index, end);
                index = end;
            } else if (ch == '"') {
                int end = skipQuoted(sql, index, '"');
                String token = unquote(sql.substring(index, end));
                boolean qualified = isSchemaQualified(sql, end);
                boolean schemaStatement = SCHEMA_STATEMENT.matcher(
                        sql.substring(0, index)).matches();
                if (token.equalsIgnoreCase(source) && (qualified || schemaStatement)) {
                    result.append(targetToken);
                } else {
                    result.append(sql, index, end);
                }
                index = end;
            } else {
                result.append(ch);
                index++;
            }
        }
        return result.toString();
    }

    /**
     * 获取 SQL 中第一个 schema 限定名。
     *
     * @param sql SQL
     * @return schema，未找到返回 null
     */
    public static String firstQualifiedSchema(String sql) {
        if (StringUtil.isBlank(sql)) {
            return null;
        }
        int index = 0;
        while (index < sql.length()) {
            char ch = sql.charAt(index);
            if (ch == '\'') {
                index = skipQuoted(sql, index, '\'');
            } else if (ch == '"') {
                int end = skipQuoted(sql, index, '"');
                if (isSchemaQualified(sql, end)) {
                    return unquote(sql.substring(index, end));
                }
                index = end;
            } else {
                index++;
            }
        }
        return null;
    }

    private static boolean isSchemaQualified(String sql, int offset) {
        int index = offset;
        while (index < sql.length() && Character.isWhitespace(sql.charAt(index))) {
            index++;
        }
        return index < sql.length() && sql.charAt(index) == '.';
    }

    private static int skipQuoted(String sql, int start, char quote) {
        int index = start + 1;
        while (index < sql.length()) {
            if (sql.charAt(index) == quote) {
                if (index + 1 < sql.length() && sql.charAt(index + 1) == quote) {
                    index += 2;
                    continue;
                }
                return index + 1;
            }
            index++;
        }
        return sql.length();
    }

    private static String quote(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private static String unquote(String identifier) {
        if (StringUtil.isBlank(identifier)) {
            return identifier;
        }
        String value = identifier.trim();
        if (value.length() >= 2 && value.charAt(0) == '"'
                && value.charAt(value.length() - 1) == '"') {
            value = value.substring(1, value.length() - 1);
        }
        return value.replace("\"\"", "\"");
    }
}
