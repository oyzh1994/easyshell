package cn.oyzh.easyshell.util.oracle;


import cn.oyzh.fx.db.DBColumnField;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;

import java.util.Locale;

/**
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleColumnUtil {

    /**
     * 初始化
     */
    public static void init() {
        DBColumnField varcharFiled = new DBColumnField("VARCHAR");
        varcharFiled.suggestSize = 255;
        varcharFiled.supportSize = true;
        varcharFiled.supportString = true;
        varcharFiled.supportDefaultValue = true;

        DBColumnField varchar2Field = new DBColumnField("VARCHAR2");
        varchar2Field.suggestSize = 255;
        varchar2Field.supportSize = true;
        varchar2Field.supportString = true;
        varchar2Field.supportDefaultValue = true;

        // ===== 字符类型 =====
        DBColumnField charFiled = new DBColumnField("CHAR");
        charFiled.suggestSize = 255;
        charFiled.supportSize = true;
        charFiled.supportString = true;
        charFiled.supportDefaultValue = true;

        DBColumnField clobField = new DBColumnField("CLOB");
        clobField.supportText = true;
        //        clobField.supportJson = true;
        clobField.supportString = true;

        DBColumnField nclobField = new DBColumnField("NCLOB");
        nclobField.supportText = true;
        nclobField.supportString = true;

        DBColumnField ncharField = new DBColumnField("NCHAR");
        ncharField.suggestSize = 255;
        ncharField.supportSize = true;
        ncharField.supportString = true;
        ncharField.supportDefaultValue = true;

        DBColumnField nvarchar2Filed = new DBColumnField("NVARCHAR2");
        nvarchar2Filed.suggestSize = 255;
        nvarchar2Filed.supportSize = true;
        nvarchar2Filed.supportString = true;
        nvarchar2Filed.supportDefaultValue = true;

        // ===== 数值类型 =====
        DBColumnField numberField = new DBColumnField("NUMBER");
        numberField.suggestSize = 10;
        numberField.supportSize = true;
        numberField.supportDigits = true;
        numberField.supportInteger = true;
        numberField.supportDefaultValue = true;
        numberField.supportAutoIncrement = true;

        DBColumnField intField = new DBColumnField("INT");
//        intField.supportSize = true;
        intField.supportInteger = true;
        intField.supportDefaultValue = true;
        intField.supportAutoIncrement = true;

        DBColumnField integerField = new DBColumnField("INTEGER");
//        integerField.supportSize = true;
        integerField.supportInteger = true;
        integerField.supportDefaultValue = true;
        integerField.supportAutoIncrement = true;

        DBColumnField bigintFiled = new DBColumnField("BIGINT");
//        bigintFiled.supportSize = true;
        bigintFiled.supportInteger = true;
        bigintFiled.supportDefaultValue = true;
        bigintFiled.supportAutoIncrement = true;

        DBColumnField smallintFiled = new DBColumnField("SMALLINT");
//        smallintFiled.supportSize = true;
        smallintFiled.supportInteger = true;
        smallintFiled.supportDefaultValue = true;
        smallintFiled.supportAutoIncrement = true;

        DBColumnField floatField = new DBColumnField("FLOAT");
        // floatField.supportSize = true;
        // floatField.supportDigits = true;
        floatField.supportDefaultValue = true;

        DBColumnField doublePrecisionField = new DBColumnField("DOUBLE PRECISION");
        // doublePrecisionField.supportSize = true;
        // doublePrecisionField.supportDigits = true;
        doublePrecisionField.supportDefaultValue = true;

        DBColumnField decimalField = new DBColumnField("DECIMAL");
        decimalField.suggestSize = 20;
        decimalField.supportSize = true;
        decimalField.supportDigits = true;
        decimalField.supportDefaultValue = true;
        decimalField.supportAutoIncrement = true;

        DBColumnField numericField = new DBColumnField("NUMERIC");
        numericField.suggestSize = 20;
        numericField.supportSize = true;
        numericField.supportDigits = true;
        numericField.supportDefaultValue = true;

        // ===== 日期时间类型 =====
        DBColumnField dateField = new DBColumnField("DATE");
        dateField.supportDefaultValue = true;

        DBColumnField timestampField = new DBColumnField("TIMESTAMP");
        timestampField.supportTimestamp = true;
        timestampField.supportDefaultValue = true;

        DBColumnField timestampWithTzField = new DBColumnField("TIMESTAMP WITH TIME ZONE");
        timestampWithTzField.alias = "DATETIME WITH TIME ZONE";
        timestampWithTzField.supportTimestamp = true;
        timestampWithTzField.supportDefaultValue = true;

        DBColumnField timestampWithLocalTzField = new DBColumnField("TIMESTAMP WITH LOCAL TIME ZONE");
        timestampWithLocalTzField.supportTimestamp = true;
        timestampWithLocalTzField.supportDefaultValue = true;

        DBColumnField intervalYmField = new DBColumnField("INTERVAL YEAR TO MONTH");
        intervalYmField.supportDefaultValue = true;
        intervalYmField.exampleValue = "INTERVAL '2-5' YEAR TO MONTH";

        DBColumnField intervalDsField = new DBColumnField("INTERVAL DAY TO SECOND");
        intervalDsField.supportDefaultValue = true;
        intervalDsField.exampleValue = "INTERVAL 'DD HH:MI:SS.FF' DAY TO SECOND";

        // ===== 二进制类型 =====
        DBColumnField blobField = new DBColumnField("BLOB");
        blobField.supportBinary = true;

        DBColumnField rawField = new DBColumnField("RAW");
        rawField.suggestSize = 255;
        rawField.supportSize = true;
        rawField.supportBinary = true;
        rawField.supportDefaultValue = true;

        DBColumnField binaryFloatField = new DBColumnField("BINARY_FLOAT");
        binaryFloatField.supportDefaultValue = true;

        DBColumnField binaryDoubleField = new DBColumnField("BINARY_DOUBLE");
        binaryDoubleField.supportDefaultValue = true;

        DBColumnField longField = new DBColumnField("LONG");
        longField.supportText = true;
        longField.supportString = true;

        DBColumnField longRawField = new DBColumnField("LONG RAW");
        longRawField.supportBinary = true;

        DBColumnField xmlTypeField = new DBColumnField("XMLTYPE");
        xmlTypeField.supportText = true;
        xmlTypeField.supportString = true;

        // ===== 注册到列表 =====

        // 字符串
        putFiled(varcharFiled);
        putFiled(varchar2Field);
        putFiled(charFiled);
        putFiled(ncharField);
        putFiled(nvarchar2Filed);

        // 数字
        putFiled(intField);
        putFiled(integerField);
        putFiled(bigintFiled);
        putFiled(smallintFiled);
        putFiled(decimalField);
        putFiled(numberField);
        putFiled(numericField);
        putFiled(floatField);
        putFiled(doublePrecisionField);

        // 时间
        putFiled(dateField);
        putFiled(timestampField);

        // 其他
        putFiled(clobField);
        putFiled(nclobField);

        // 二进制
        putFiled(blobField);
        putFiled(rawField);
        putFiled(binaryFloatField);
        putFiled(binaryDoubleField);
        putFiled(longField);
        putFiled(longRawField);
        putFiled(xmlTypeField);

        // 不常见
        putFiled(timestampWithTzField);
        putFiled(timestampWithLocalTzField);
        putFiled(intervalYmField);
        putFiled(intervalDsField);
    }

    private static void putFiled(DBColumnField columnField) {
        DBColumnFieldManager.putFiled(DBDialect.ORACLE, columnField);
    }

    public static Object defaultValue(String type) {
        if (DBColumnFieldManager.supportDefaultValue(DBDialect.ORACLE, type)) {
            if (DBColumnFieldManager.supportDigits(DBDialect.ORACLE, type)) {
                return 0.0;
            }
            if (DBColumnFieldManager.supportInteger(DBDialect.ORACLE, type)) {
                return 0;
            }
            if (DBColumnFieldManager.supportString(DBDialect.ORACLE, type)) {
                return "";
            }
            if (DBColumnFieldManager.supportJson(DBDialect.ORACLE, type)) {
                return "{'a':1}";
            }
            if (DBColumnFieldManager.supportBinary(DBDialect.ORACLE, type)) {
                return new byte[]{};
            }
        }
        return null;
    }

    /**
     * 获取Oracle中省略长度时必须补写的默认长度。
     *
     * @param type 字段类型
     * @return 默认长度，无须长度时返回null
     */
    public static Integer requiredSize(String type) {
        if (type == null) {
            return null;
        }
        String normalized = type.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "CHAR", "VARCHAR", "VARCHAR2", "NCHAR", "NVARCHAR2", "RAW" -> 255;
            default -> null;
        };
    }

    public static boolean isYearType(String type) {
        return "YEAR".equalsIgnoreCase(type);
    }

    public static boolean isDateType(String type) {
        return "DATE".equalsIgnoreCase(type);
    }

    public static boolean isDateTimeType(String type) {
        if (type == null) {
            return false;
        }
        String normalized = type.trim().toUpperCase(Locale.ROOT);
        return normalized.startsWith("TIMESTAMP") || "DATETIME".equals(normalized);
    }

    public static boolean isTimeType(String type) {
        return "TIME".equalsIgnoreCase(type);
    }
}
