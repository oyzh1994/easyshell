package cn.oyzh.easyshell.db.type;

import cn.oyzh.easyshell.db.index.ShellIndexHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 类型工具类
 * <p>
 * 类型设计以“定义”的方式编辑，这里提供与方言无关的语句判定与名称解析。
 * 兼容 CREATE TYPE / CREATE TYPE BODY / CREATE DOMAIN / ALTER TYPE / CREATE OR REPLACE TYPE。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTypeHelper {

    /**
     * 创建/修改类型语句匹配模式
     * <p>兼容 CREATE OR REPLACE、EDITIONABLE、IF NOT EXISTS、TYPE BODY、DOMAIN
     */
    private static final Pattern CREATE_TYPE_PATTERN = Pattern.compile(
            "(?is)^\\s*(?:CREATE|ALTER)\\s+(?:OR\\s+REPLACE\\s+)?"
                    + "(?:EDITIONABLE\\s+|NONEDITIONABLE\\s+)?"
                    + "(?:TYPE|DOMAIN|CLASS)\\s+(?:BODY\\s+)?"
                    + "(?:IF\\s+NOT\\s+EXISTS\\s+)?([^\\s(]+)");

    /**
     * 删除类型语句匹配模式
     */
    private static final Pattern DROP_TYPE_PATTERN = Pattern.compile(
            "(?is)^\\s*DROP\\s+(?:TYPE|DOMAIN|CLASS)\\s+(?:BODY\\s+)?");

    /**
     * 原地替换/修改语句匹配模式（此类语句无需先删除原类型）
     */
    private static final Pattern REPLACE_TYPE_PATTERN = Pattern.compile(
            "(?is)^\\s*(?:CREATE\\s+OR\\s+REPLACE\\s+(?:EDITIONABLE\\s+|NONEDITIONABLE\\s+)?"
                    + "(?:TYPE|DOMAIN|CLASS)\\b|ALTER\\s+(?:TYPE|DOMAIN|CLASS)\\b)");

    /**
     * 是否创建/修改类型语句
     *
     * @param sql SQL语句
     * @return 是否创建/修改类型语句
     */
    public static boolean isCreateTypeSql(String sql) {
        return sql != null && CREATE_TYPE_PATTERN.matcher(sql).find();
    }

    /**
     * 是否删除类型语句
     *
     * @param sql SQL语句
     * @return 是否删除类型语句
     */
    public static boolean isDropTypeSql(String sql) {
        return sql != null && DROP_TYPE_PATTERN.matcher(sql).find();
    }

    /**
     * 是否原地替换/修改语句
     *
     * @param sql SQL语句
     * @return 是否原地替换/修改语句
     */
    public static boolean isReplaceTypeSql(String sql) {
        return sql != null && REPLACE_TYPE_PATTERN.matcher(sql).find();
    }

    /**
     * 解析创建/修改类型语句中的类型名称
     *
     * @param sql SQL语句
     * @return 类型名称，未解析到返回null
     */
    public static String parseTypeName(String sql) {
        if (sql == null) {
            return null;
        }
        Matcher matcher = CREATE_TYPE_PATTERN.matcher(sql);
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
