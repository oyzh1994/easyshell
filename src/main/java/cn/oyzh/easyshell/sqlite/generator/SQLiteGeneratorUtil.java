package cn.oyzh.easyshell.sqlite.generator;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

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
