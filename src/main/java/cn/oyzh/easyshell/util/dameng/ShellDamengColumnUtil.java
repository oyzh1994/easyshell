package cn.oyzh.easyshell.util.dameng;


import cn.oyzh.fx.db.DBColumnField;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;

/**
 * @author oyzh
 * @since 2024/1/29
 */
public class ShellDamengColumnUtil {

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

        DBColumnField intField = new DBColumnField("INT");
        intField.supportInteger = true;
        intField.supportDefaultValue = true;
        intField.supportAutoIncrement = true;

        DBColumnField integerField = new DBColumnField("INTEGER");
        integerField.supportInteger = true;
        integerField.supportDefaultValue = true;
        integerField.supportAutoIncrement = true;

        DBColumnField bigintFiled = new DBColumnField("BIGINT");
        bigintFiled.supportInteger = true;
        bigintFiled.supportDefaultValue = true;
        bigintFiled.supportAutoIncrement = true;

        DBColumnField smallintFiled = new DBColumnField("SMALLINT");
        smallintFiled.supportInteger = true;
        smallintFiled.supportDefaultValue = true;
        smallintFiled.supportAutoIncrement = true;

        DBColumnField tinyintField = new DBColumnField("TINYINT");
        tinyintField.supportInteger = true;
        tinyintField.supportDefaultValue = true;
        tinyintField.supportAutoIncrement = true;

        DBColumnField floatField = new DBColumnField("FLOAT");
        // floatField.supportSize = true;
        // floatField.supportDigits = true;
        floatField.supportDefaultValue = true;

        DBColumnField doubleField = new DBColumnField("DOUBLE");
        // doubleField.supportSize = true;
        // doubleField.supportDigits = true;
        doubleField.supportDefaultValue = true;

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

        DBColumnField datetimeField = new DBColumnField("DATETIME");
        datetimeField.supportTimestamp = true;
        datetimeField.supportDefaultValue = true;

        DBColumnField timeField = new DBColumnField("TIME");
        timeField.supportTimestamp = true;
        timeField.supportDefaultValue = true;

        DBColumnField timeWithTzField = new DBColumnField("TIME WITH TIME ZONE");
        timeWithTzField.supportTimestamp = true;
        timeWithTzField.supportDefaultValue = true;

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

        DBColumnField imageField = new DBColumnField("IMAGE");
        imageField.supportBinary = true;

        DBColumnField rawField = new DBColumnField("RAW");
        rawField.suggestSize = 255;
        // rawField.supportSize = true;
        rawField.supportBinary = true;
        rawField.supportDefaultValue = true;

        DBColumnField varbinaryField = new DBColumnField("VARBINARY");
        varbinaryField.suggestSize = 8189;
        varbinaryField.supportBinary = true;
        varbinaryField.supportDefaultValue = true;

        DBColumnField binaryField = new DBColumnField("BINARY");
        binaryField.suggestSize = 255;
        binaryField.supportSize = true;
        binaryField.supportBinary = true;
        binaryField.supportDefaultValue = true;

        // ===== 位类型 =====
        DBColumnField bitFiled = new DBColumnField("BIT");
        bitFiled.minValue = 0L;
        bitFiled.maxValue = 1L;
        bitFiled.supportBoolean = true;
        bitFiled.supportDefaultValue = true;

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
        putFiled(tinyintField);
        putFiled(decimalField);
        putFiled(numberField);
        putFiled(numericField);
        putFiled(floatField);
        putFiled(doubleField);

        // 时间
        putFiled(timeField);
        putFiled(dateField);
        putFiled(datetimeField);
        putFiled(timestampField);

        // 其他
        putFiled(bitFiled);
        putFiled(clobField);

        // 二进制
        putFiled(blobField);
        putFiled(varbinaryField);
        putFiled(binaryField);
        putFiled(rawField);
        putFiled(imageField);

        // 不常见
        putFiled(timeWithTzField);
        putFiled(timestampWithTzField);
        putFiled(timestampWithLocalTzField);
        putFiled(intervalYmField);
        putFiled(intervalDsField);
        putFiled(doublePrecisionField);
    }

    private static void putFiled(DBColumnField columnField) {
        DBColumnFieldManager.putFiled(DBDialect.DAMENG, columnField);
    }

    public static Object defaultValue(String type) {
        if (DBColumnFieldManager.supportDefaultValue(DBDialect.DAMENG, type)) {
            if (DBColumnFieldManager.supportDigits(DBDialect.DAMENG, type)) {
                return 0.0;
            }
            if (DBColumnFieldManager.supportInteger(DBDialect.DAMENG, type)) {
                return 0;
            }
            if (DBColumnFieldManager.supportString(DBDialect.DAMENG, type)) {
                return "";
            }
            if (DBColumnFieldManager.supportJson(DBDialect.DAMENG, type)) {
                return "{'a':1}";
            }
            if (DBColumnFieldManager.supportBinary(DBDialect.DAMENG, type)) {
                return new byte[]{};
            }
        }
        return null;
    }

    public static boolean isYearType(String type) {
        return "YEAR".equalsIgnoreCase(type);
    }

    public static boolean isDateType(String type) {
        return "DATE".equalsIgnoreCase(type);
    }

    public static boolean isDateTimeType(String type) {
        return "DATETIME".equalsIgnoreCase(type);
    }

    public static boolean isTimeType(String type) {
        return "TIME".equalsIgnoreCase(type);
    }
}
