//package cn.oyzh.fx.db;
//
///**
// *
// * @author oyzh
// * @since 2026-08-21
// */
//public interface DBColumn {
//
//    /**
//     * 获取名称
//     *
//     * @return 结果
//     */
//    String getName();
//
//    /**
//     * 设置名称
//     *
//     * @param name 名称
//     */
//    void setName(String name);
//
//    /**
//     * 获取类型
//     *
//     * @return 结果
//     */
//    String getType();
//
//    /**
//     * 设置长度
//     *
//     * @param size 长度
//     */
//    void setSize(Integer size);
//
//    /**
//     * 获取长度
//     *
//     * @return 结果
//     */
//    Integer getSize();
//
//    /**
//     * 设置类型
//     *
//     * @param type 类型
//     */
//    void setType(String type);
//
//    /**
//     * 是否支持长度
//     *
//     * @return 结果
//     */
//    default boolean supportSize() {
//        return false;
//    }
//
//    /**
//     * 是否支持几何
//     *
//     * @return 结果
//     */
//    default boolean supportGeometry() {
//        return false;
//    }
//
//    /**
//     * 是否支持字符集
//     *
//     * @return 结果
//     */
//    default boolean supportCharset() {
//        return false;
//    }
//
//    /**
//     * 是否支持无符号
//     *
//     * @return 结果
//     */
//    default boolean supportUnsigned() {
//        return false;
//    }
//
//    /**
//     * 是否支持小数
//     *
//     * @return 结果
//     */
//    default boolean supportDigits() {
//        return false;
//    }
//
//    /**
//     * 是否支持整数
//     *
//     * @return 结果
//     */
//    default boolean supportInteger() {
//        return false;
//    }
//
//    /**
//     * 是否支持长整数
//     *
//     * @return 结果
//     */
//    default boolean supportBigInteger() {
//        return false;
//    }
//
//    /**
//     * 是否支持自动递增
//     *
//     * @return 结果
//     */
//    default boolean supportAutoIncrement() {
//        return false;
//    }
//
//    /**
//     * 是否支持默认值
//     *
//     * @return 结果
//     */
//    default boolean supportDefaultValue() {
//        return false;
//    }
//
//    /**
//     * 是否支持时间戳
//     *
//     * @return 结果
//     */
//    default boolean supportTimestamp() {
//        return false;
//    }
//
//    /**
//     * 是否支持值
//     *
//     * @return 结果
//     */
//    default boolean supportValue() {
//        return false;
//    }
//
//    /**
//     * 是否支持文本
//     *
//     * @return 结果
//     */
//    default boolean supportText() {
//        return false;
//    }
//
//    /**
//     * 是否支持填充零
//     *
//     * @return 结果
//     */
//    default boolean supportZeroFill() {
//        return false;
//    }
//
//    /**
//     * 是否支持填充零
//     *
//     * @return 结果
//     */
//    default boolean supportBit() {
//        return false;
//    }
//
//    /**
//     * 是否支持json
//     *
//     * @return 结果
//     */
//    default boolean supportJson() {
//        return false;
//    }
//
//    /**
//     * 是否支持json数组
//     *
//     * @return 结果
//     */
//    default boolean supportJsonArray() {
//        return false;
//    }
//
//    /**
//     * 是否支持键长度
//     *
//     * @return 结果
//     */
//    default boolean supportKeySize() {
//        return false;
//    }
//
//    /**
//     * 是否支持字符串
//     *
//     * @return 结果
//     */
//    default boolean supportString() {
//        return false;
//    }
//
//    /**
//     * 是否支持布尔
//     *
//     * @return 结果
//     */
//    default boolean supportBoolean() {
//        return false;
//    }
//
//    /**
//     * 是否支持二进制
//     *
//     * @return 结果
//     */
//    default boolean supportBinary() {
//        return false;
//    }
//
//    /**
//     * 是否支持枚举
//     *
//     * @return 结果
//     */
//    default boolean supportEnum() {
//        return false;
//    }
//
//    /**
//     * 获取最小值
//     *
//     * @return 结果
//     */
//    default Long minValue() {
//        return null;
//    }
//
//    /**
//     * 获取最大值
//     *
//     * @return 结果
//     */
//    default Long maxValue() {
//        return null;
//    }
//
//    /**
//     * 获取示例值
//     *
//     * @return 结果
//     */
//    default Object exampleValue() {
//        return null;
//    }
//}
