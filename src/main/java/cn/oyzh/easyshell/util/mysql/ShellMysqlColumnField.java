package cn.oyzh.easyshell.util.mysql;

/**
 * mysql字段域
 *
 * @author oyzh
 * @since 2025-11-13
 */
public class ShellMysqlColumnField {

    /**
     * 名称
     */
    public String name;

    /**
     * 别名
     */
    public String alias;

    public Long maxValue;

    public Long minValue;

    /**
     * 推荐字段长
     */
    public Integer suggestSize;

    public boolean supportBit;

    public String exampleValue;

    public boolean supportSize;

    public boolean supportJson;

    public boolean supportEnum;

    public boolean supportValue;

    public boolean supportBinary;

    public boolean supportDigits;

    public boolean supportString;

    public boolean supportKeySize;

    public boolean supportInteger;

    public boolean supportCharset;

    public boolean supportUnsigned;

    public boolean supportZeroFill;

    public boolean supportGeometry;

    public boolean supportTimestamp;

    public boolean supportDefaultValue;

    public boolean supportAutoIncrement;

    public ShellMysqlColumnField(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}