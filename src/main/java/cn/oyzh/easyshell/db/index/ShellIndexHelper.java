package cn.oyzh.easyshell.db.index;

import cn.oyzh.common.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 索引工具类
 * <p>
 * 索引设计以“定义”的方式编辑，这里提供与方言无关的语句判定与名称解析。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellIndexHelper {

    /**
     * 创建索引语句匹配模式
     */
    private static final Pattern CREATE_INDEX_PATTERN = Pattern.compile(
            "(?is)^\\s*CREATE\\s+(?:UNIQUE\\s+|BITMAP\\s+|CLUSTERED\\s+|NONCLUSTERED\\s+|FULLTEXT\\s+|SPATIAL\\s+)*"
                    + "INDEX\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([^\\s(]+)");

    /**
     * 删除索引语句匹配模式
     */
    private static final Pattern DROP_INDEX_PATTERN = Pattern.compile(
            "(?is)^\\s*DROP\\s+INDEX\\s+");

    /**
     * 是否创建索引语句
     *
     * @param sql SQL语句
     * @return 是否创建索引语句
     */
    public static boolean isCreateIndexSql(String sql) {
        return sql != null && CREATE_INDEX_PATTERN.matcher(sql).find();
    }

    /**
     * 是否删除索引语句
     *
     * @param sql SQL语句
     * @return 是否删除索引语句
     */
    public static boolean isDropIndexSql(String sql) {
        return sql != null && DROP_INDEX_PATTERN.matcher(sql).find();
    }

    /**
     * 解析创建索引语句中的索引名称
     *
     * @param sql SQL语句
     * @return 索引名称，未解析到返回null
     */
    public static String parseIndexName(String sql) {
        if (sql == null) {
            return null;
        }
        Matcher matcher = CREATE_INDEX_PATTERN.matcher(sql);
        if (!matcher.find()) {
            return null;
        }
        String name = matcher.group(1).strip();
        // 去掉模式前缀
        int offset = name.lastIndexOf('.');
        if (offset >= 0) {
            name = name.substring(offset + 1);
        }
        // 去掉标识符的包裹符
        return name.replace("\"", "").replace("`", "")
                .replace("[", "").replace("]", "").strip();
    }

    /**
     * 移除定义末尾的语句结束符
     *
     * @param definition 定义
     * @return 定义
     */
    public static String stripTerminator(String definition) {
        if (StringUtil.isBlank(definition)) {
            return definition;
        }
        String result = definition.strip();
        while (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1).stripTrailing();
        }
        return result;
    }
}
