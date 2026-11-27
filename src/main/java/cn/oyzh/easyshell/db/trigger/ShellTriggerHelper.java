package cn.oyzh.easyshell.db.trigger;

import cn.oyzh.easyshell.db.index.ShellIndexHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 触发器工具类
 * <p>
 * 触发器设计以“定义”的方式编辑，这里提供与方言无关的语句判定与名称解析。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTriggerHelper {

    /**
     * 创建/修改触发器语句匹配模式
     * <p>兼容 CREATE OR REPLACE、EDITIONABLE、DEFINER、TEMP、CONSTRAINT、IF NOT EXISTS、ALTER TRIGGER
     */
    private static final Pattern CREATE_TRIGGER_PATTERN = Pattern.compile(
            "(?is)^\\s*(?:CREATE|ALTER)\\s+(?:OR\\s+REPLACE\\s+)?"
                    + "(?:EDITIONABLE\\s+|NONEDITIONABLE\\s+)?"
                    + "(?:DEFINER\\s*=\\s*\\S+\\s+)?"
                    + "(?:TEMP(?:ORARY)?\\s+)?"
                    + "(?:CONSTRAINT\\s+)?"
                    + "TRIGGER\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([^\\s(]+)");

    /**
     * 删除触发器语句匹配模式
     */
    private static final Pattern DROP_TRIGGER_PATTERN = Pattern.compile(
            "(?is)^\\s*DROP\\s+TRIGGER\\s+(?:IF\\s+EXISTS\\s+)?");

    /**
     * 原地替换/修改语句匹配模式（此类语句无需先删除原触发器）
     */
    private static final Pattern REPLACE_TRIGGER_PATTERN = Pattern.compile(
            "(?is)^\\s*(?:CREATE\\s+OR\\s+REPLACE\\s+(?:EDITIONABLE\\s+|NONEDITIONABLE\\s+)?"
                    + "(?:DEFINER\\s*=\\s*\\S+\\s+)?TRIGGER\\b|ALTER\\s+TRIGGER\\b)");

    /**
     * 是否创建/修改触发器语句
     *
     * @param sql SQL语句
     * @return 是否创建/修改触发器语句
     */
    public static boolean isCreateTriggerSql(String sql) {
        return sql != null && CREATE_TRIGGER_PATTERN.matcher(sql).find();
    }

    /**
     * 是否删除触发器语句
     *
     * @param sql SQL语句
     * @return 是否删除触发器语句
     */
    public static boolean isDropTriggerSql(String sql) {
        return sql != null && DROP_TRIGGER_PATTERN.matcher(sql).find();
    }

    /**
     * 是否原地替换/修改触发器语句
     *
     * @param sql SQL语句
     * @return 是否原地替换/修改触发器语句
     */
    public static boolean isReplaceTriggerSql(String sql) {
        return sql != null && REPLACE_TRIGGER_PATTERN.matcher(sql).find();
    }

    /**
     * 解析创建/修改触发器语句中的触发器名称
     *
     * @param sql SQL语句
     * @return 触发器名称，未解析到返回null
     */
    public static String parseTriggerName(String sql) {
        if (sql == null) {
            return null;
        }
        Matcher matcher = CREATE_TRIGGER_PATTERN.matcher(sql);
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
        return ShellIndexHelper.stripTerminator(definition);
    }
}
