package cn.oyzh.easyshell.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.i18n.I18nHelper;

/**
 * redis键类型
 *
 * @author oyzh
 * @since 2023/07/01
 */
public enum ShellRedisKeyType {
    /**
     * 字符串
     */
    STRING(),
    /**
     * 集合
     */
    SET(),
    /**
     * 有序集合
     */
    ZSET(),
    /**
     * 列表
     */
    LIST(),
    /**
     * 哈希表
     */
    HASH(),
    /**
     * 流
     */
    STREAM(),
    /**
     * json
     */
    JSON();

    /**
     * 获取描述
     *
     * @return 描述
     */
    public String desc() {
        // if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE) {
        return switch (this) {
            case STRING -> I18nHelper.string();
            case LIST -> I18nHelper.list();
            case SET -> I18nHelper.set1();
            case ZSET -> I18nHelper.zset();
            case HASH -> I18nHelper.hash();
            case STREAM -> I18nHelper.stream();
            case JSON -> I18nHelper.json();
        };
        // } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE) {
        //     return switch (this) {
        //         case STRING -> "字符串";
        //         case SET -> "集合";
        //         case ZSET -> "有序集合";
        //         case LIST -> "列表";
        //         case HASH -> "哈希表";
        //         case STREAM -> "流";
        //     };
        // } else {
        //     return switch (this) {
        //         case STRING -> "String";
        //         case SET -> "Set";
        //         case ZSET -> "ZSet";
        //         case LIST -> "List";
        //         case HASH -> "Hash";
        //         case STREAM -> "Stream";
        //     };
        // }
    }

    /**
     * 构造方法
     */
    ShellRedisKeyType() {
    }

    /**
     * 根据字符串获取键类型
     *
     * @param type 类型字符串
     * @return 键类型
     */
    public static ShellRedisKeyType valueOfType(String type) {
        if (StringUtil.isNotBlank(type)) {
            return switch (type.toLowerCase()) {
                case "string", "bitmap", "hyperloglog", "hylog" -> STRING;
                case "set" -> SET;
                case "zset", "geo" -> ZSET;
                case "list" -> LIST;
                case "hash" -> HASH;
                case "stream" -> STREAM;
                case "rejson-rl", "json" -> JSON;
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
        return valueOfType(type) != null;
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
