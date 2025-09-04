package cn.oyzh.easyshell.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.i18n.I18nManager;

import java.util.Locale;

/**
 * redis键类型
 *
 * @author oyzh
 * @since 2023/07/01
 */
public enum ShellRedisKeyType {
    STRING(),
    SET(),
    ZSET(),
    LIST(),
    HASH(),
    STREAM();

    public String desc() {
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE) {
            return switch (this) {
                case STRING -> "字符串";
                case SET -> "集合";
                case ZSET -> "有序集合";
                case LIST -> "列表";
                case HASH -> "哈希表";
                case STREAM -> "流";
            };

        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE) {
            return switch (this) {
                case STRING -> "字符串";
                case SET -> "集合";
                case ZSET -> "有序集合";
                case LIST -> "列表";
                case HASH -> "哈希表";
                case STREAM -> "流";
            };
        } else {
            return switch (this) {
                case STRING -> "String";
                case SET -> "Set";
                case ZSET -> "ZSet";
                case LIST -> "List";
                case HASH -> "Hash";
                case STREAM -> "Stream";
            };
        }
    }

    ShellRedisKeyType() {
    }

    public static ShellRedisKeyType valueOfType(String type) {
        if (StringUtil.isNotBlank(type)) {
            return switch (type.toLowerCase()) {
                case "string", "bitmap", "hyperloglog", "hylog" -> STRING;
                case "set" -> SET;
                case "zset", "geo" -> ZSET;
                case "list" -> LIST;
                case "hash" -> HASH;
                case "stream" -> STREAM;
                default -> null;
            };
        }
        return null;
    }

    /**
     * 跟字符串比较
     *
     * @param type 字符串类型
     * @return 结果
     */
    public boolean equalsString(String type) {
        return StringUtil.equalsIgnoreCase(type, this.name());
    }

    /**
     * 枚举长度
     *
     * @return 长度
     */
    public static int length() {
        return ShellRedisKeyType.values().length;
    }
}
