package cn.oyzh.easyshell.db;

/**
 * 字段域
 *
 * @author oyzh
 * @since 2025-11-13
 */
public class DBColumnField {

    /**
     * 名称
     */
    public String name;

    /**
     * 别名
     */
    public String alias;

    /**
     * 最大值
     */
    public Long maxValue;

    /**
     * 最小值
     */
    public Long minValue;

    /**
     * 推荐字段长
     */
    public Integer suggestSize;

    /**
     * 是否支持bit类型
     */
    public boolean supportBit;

    /**
     * 示例值
     */
    public String exampleValue;

    /**
     * 是否支持大小
     */
    public boolean supportSize;

    /**
     * 是否支持json
     */
    public boolean supportJson;

    /**
     * 是否支持json数组
     */
    public boolean supportJsonArray;

    /**
     * 是否支持文本
     */
    public boolean supportText;

    /**
     * 是否支持枚举
     */
    public boolean supportEnum;

    /**
     * 是否支持值
     */
    public boolean supportValue;

    /**
     * 是否支持二进制
     */
    public boolean supportBinary;

    /**
     * 是否支持小数
     */
    public boolean supportDigits;

    /**
     * 是否支持字符串
     */
    public boolean supportString;

    /**
     * 是否支持boolean
     */
    public boolean supportBoolean;

    /**
     * 是否支持键长度
     */
    public boolean supportKeySize;

    /**
     * 是否支持整数
     */
    public boolean supportInteger;

    /**
     * 是否支持长整数
     */
    public boolean supportBigInteger;

    /**
     * 是否支持字符集
     */
    public boolean supportCharset;

    /**
     * 是否支持无符号
     */
    public boolean supportUnsigned;

    /**
     * 是否支持填充0
     */
    public boolean supportZeroFill;

    /**
     * 是否支持集合
     */
    public boolean supportGeometry;

    /**
     * 是否支持时间戳
     */
    public boolean supportTimestamp;

    /**
     * 是否支持默认值
     */
    public boolean supportDefaultValue;

    /**
     * 是否支持自动递增
     */
    public boolean supportAutoIncrement;

    public DBColumnField(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
