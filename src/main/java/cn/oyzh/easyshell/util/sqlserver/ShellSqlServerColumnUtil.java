package cn.oyzh.easyshell.util.sqlserver;


import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBColumnField;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;


/**
 * SqlServer字段类型工具类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerColumnUtil {

    private static boolean initialized;

    /**
     * 初始化
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        // ===== SQL Server 原生类型 =====
        DBColumnField nvarcharField = new DBColumnField("NVARCHAR");
        nvarcharField.suggestSize = 255;
        nvarcharField.supportSize = true;
        nvarcharField.supportString = true;
        nvarcharField.supportDefaultValue = true;

        DBColumnField ncharField = new DBColumnField("NCHAR");
        ncharField.suggestSize = 1;
        ncharField.supportSize = true;
        ncharField.supportString = true;
        ncharField.supportDefaultValue = true;

        DBColumnField uniqueIdentifierField = new DBColumnField("UNIQUEIDENTIFIER");
        uniqueIdentifierField.supportString = true;
        uniqueIdentifierField.supportDefaultValue = true;
        uniqueIdentifierField.exampleValue = "00000000-0000-0000-0000-000000000000";

        DBColumnField sqlVariantField = new DBColumnField("SQL_VARIANT");
        sqlVariantField.supportString = true;

        DBColumnField tinyintField = new DBColumnField("TINYINT");
        tinyintField.minValue = 0L;
        tinyintField.maxValue = 255L;
        tinyintField.supportInteger = true;
        tinyintField.supportDefaultValue = true;
        tinyintField.supportAutoIncrement = true;

        DBColumnField decimalField = new DBColumnField("DECIMAL");
        decimalField.alias = "DEC";
        decimalField.suggestSize = 18;
        decimalField.supportSize = true;
        decimalField.supportDigits = true;
        decimalField.supportDefaultValue = true;

        DBColumnField floatField = new DBColumnField("FLOAT");
        floatField.supportDigits = true;
        floatField.supportDefaultValue = true;

        DBColumnField smallMoneyField = new DBColumnField("SMALLMONEY");
        smallMoneyField.supportDigits = true;
        smallMoneyField.supportDefaultValue = true;

        DBColumnField datetimeField = new DBColumnField("DATETIME");
        datetimeField.supportTimestamp = true;
        datetimeField.supportDefaultValue = true;

        DBColumnField datetime2Field = new DBColumnField("DATETIME2");
        datetime2Field.supportSize = true;
        datetime2Field.suggestSize = 7;
        datetime2Field.supportTimestamp = true;
        datetime2Field.supportDefaultValue = true;

        DBColumnField datetimeOffsetField = new DBColumnField("DATETIMEOFFSET");
        datetimeOffsetField.supportSize = true;
        datetimeOffsetField.suggestSize = 7;
        datetimeOffsetField.supportTimestamp = true;
        datetimeOffsetField.supportDefaultValue = true;

        DBColumnField smallDatetimeField = new DBColumnField("SMALLDATETIME");
        smallDatetimeField.supportTimestamp = true;
        smallDatetimeField.supportDefaultValue = true;

        DBColumnField binaryField = new DBColumnField("BINARY");
        binaryField.suggestSize = 1;
        binaryField.supportSize = true;
        binaryField.supportBinary = true;
        binaryField.supportDefaultValue = true;

        DBColumnField varbinaryField = new DBColumnField("VARBINARY");
        varbinaryField.suggestSize = 255;
        varbinaryField.supportSize = true;
        varbinaryField.supportBinary = true;
        varbinaryField.supportDefaultValue = true;

        DBColumnField imageField = new DBColumnField("IMAGE");
        imageField.supportBinary = true;

        DBColumnField rowVersionField = new DBColumnField("ROWVERSION");
        rowVersionField.supportBinary = true;

        // SQL Server 的 TIMESTAMP 是 ROWVERSION 的别名，不是日期时间。
        DBColumnField sqlServerTimestampField = new DBColumnField("TIMESTAMP");
        sqlServerTimestampField.supportBinary = true;

        DBColumnField hierarchyIdField = new DBColumnField("HIERARCHYID");
        hierarchyIdField.supportBinary = true;

        DBColumnField geometryField = new DBColumnField("GEOMETRY");
        geometryField.supportBinary = true;

        DBColumnField geographyField = new DBColumnField("GEOGRAPHY");
        geographyField.supportBinary = true;

        // ===== 字符类型 =====
        DBColumnField varcharField = new DBColumnField("VARCHAR");
        varcharField.suggestSize = 255;
        varcharField.supportSize = true;
        varcharField.supportString = true;
        varcharField.supportDefaultValue = true;

        DBColumnField charField = new DBColumnField("CHAR");
        charField.suggestSize = 1;
        charField.supportSize = true;
        charField.supportString = true;
        charField.supportDefaultValue = true;

        DBColumnField textField = new DBColumnField("TEXT");
        textField.supportText = true;
        textField.supportDefaultValue = true;

        DBColumnField ntextField = new DBColumnField("NTEXT");
        ntextField.supportText = true;
        ntextField.supportDefaultValue = true;

        DBColumnField xmlField = new DBColumnField("XML");
        xmlField.supportText = true;
        xmlField.supportDefaultValue = true;

        // ===== 数值类型 =====
        DBColumnField smallintField = new DBColumnField("SMALLINT");
        smallintField.minValue = -32768L;
        smallintField.maxValue = 32767L;
        smallintField.supportInteger = true;
        smallintField.supportDefaultValue = true;
        smallintField.supportAutoIncrement = true;

        DBColumnField integerField = new DBColumnField("INTEGER");
        integerField.minValue = (long) Integer.MIN_VALUE;
        integerField.maxValue = (long) Integer.MAX_VALUE;
        integerField.supportInteger = true;
        integerField.supportDefaultValue = true;
        integerField.supportAutoIncrement = true;

        DBColumnField intField = new DBColumnField("INT");
        intField.minValue = (long) Integer.MIN_VALUE;
        intField.maxValue = (long) Integer.MAX_VALUE;
        intField.supportInteger = true;
        intField.supportDefaultValue = true;
        intField.supportAutoIncrement = true;

        DBColumnField bigintField = new DBColumnField("BIGINT");
        bigintField.minValue = Long.MIN_VALUE;
        bigintField.maxValue = Long.MAX_VALUE;
        bigintField.supportInteger = true;
        bigintField.supportDefaultValue = true;
        bigintField.supportAutoIncrement = true;

        DBColumnField numericField = new DBColumnField("NUMERIC");
        numericField.alias = "DECIMAL";
        numericField.suggestSize = 20;
        numericField.supportSize = true;
        numericField.supportDigits = true;
        numericField.supportDefaultValue = true;

        DBColumnField realField = new DBColumnField("REAL");
        realField.supportDigits = true;
        realField.supportDefaultValue = true;

        DBColumnField moneyField = new DBColumnField("MONEY");
        moneyField.supportDigits = true;
        moneyField.supportDefaultValue = true;
        moneyField.exampleValue = "0.00";

        // ===== 日期时间类型 =====
        DBColumnField dateField = new DBColumnField("DATE");
        dateField.supportDefaultValue = true;

        DBColumnField timeField = new DBColumnField("TIME");
        timeField.supportSize = true;
        timeField.suggestSize = 7;
        timeField.supportDefaultValue = true;

        // ===== 位串类型 =====
        DBColumnField bitField = new DBColumnField("BIT");
        // SQL Server的BIT是布尔值，不是PostgreSQL的位串
        bitField.supportBoolean = true;
        bitField.supportDefaultValue = true;
        bitField.exampleValue = "1";

        // ===== 注册到列表 =====

        // 常用类型优先
        putFiled(intField);
        putFiled(bigintField);
        putFiled(nvarcharField);
        putFiled(varcharField);
        putFiled(decimalField);
        putFiled(bitField);
        putFiled(datetime2Field);
        putFiled(dateField);
        putFiled(datetimeField);
        putFiled(uniqueIdentifierField);

        // 其他常用数值和字符类型
        putFiled(smallintField);
        putFiled(tinyintField);
        putFiled(floatField);
        putFiled(ncharField);
        putFiled(charField);
        putFiled(moneyField);
        putFiled(timeField);
        putFiled(datetimeOffsetField);
        putFiled(numericField);
        putFiled(realField);
        putFiled(smallMoneyField);
        putFiled(smallDatetimeField);

        // 二进制类型
        putFiled(varbinaryField);
        putFiled(binaryField);

        // 低频、兼容和空间类型
        putFiled(xmlField);
        putFiled(textField);
        putFiled(ntextField);
        putFiled(sqlVariantField);
        putFiled(imageField);
        putFiled(rowVersionField);
        putFiled(sqlServerTimestampField);
        putFiled(integerField);
        putFiled(hierarchyIdField);
        putFiled(geometryField);
        putFiled(geographyField);

    }

    private static void putFiled(DBColumnField columnField) {
        DBColumnFieldManager.putFiled(DBDialect.SQLSERVER, columnField);
    }

    public static Object defaultValue(String type) {
        if (DBColumnFieldManager.supportDefaultValue(DBDialect.SQLSERVER, type)) {
            if (DBColumnFieldManager.supportDigits(DBDialect.SQLSERVER, type)) {
                return 0.0;
            }
            if (DBColumnFieldManager.supportInteger(DBDialect.SQLSERVER, type)) {
                return 0;
            }
            if (DBColumnFieldManager.supportBoolean(DBDialect.SQLSERVER, type)) {
                return "1";
            }
            if (DBColumnFieldManager.supportString(DBDialect.SQLSERVER, type)) {
                return "";
            }
            if (DBColumnFieldManager.supportJson(DBDialect.SQLSERVER, type)) {
                return "{}";
            }
            if (DBColumnFieldManager.supportBinary(DBDialect.SQLSERVER, type)) {
                return new byte[]{};
            }
        }
        return null;
    }

    public static boolean isYearType(String type) {
        // SqlServer没有YEAR类型
        return false;
    }

    public static boolean isDateType(String type) {
        return StringUtil.equalsIgnoreCase("DATE", type);
    }

    public static boolean isDateTimeType(String type) {
        return StringUtil.equalsAnyIgnoreCase(type,
                "DATETIME",
                "DATETIME2",
                "DATETIMEOFFSET",
                "SMALLDATETIME");
    }

    public static boolean isTimeType(String type) {
        return StringUtil.equalsAnyIgnoreCase(type, "TIME");
    }

    /**
     * SQL Server不支持PostgreSQL的SERIAL系列伪类型。
     */
    public static boolean isSerialType(String type) {
        return false;
    }

    public static String serialTypeFor(String type) {
        return null;
    }

    public static String serialBaseType(String type) {
        return type;
    }
}
