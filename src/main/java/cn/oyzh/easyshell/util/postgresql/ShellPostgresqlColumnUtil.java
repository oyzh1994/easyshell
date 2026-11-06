package cn.oyzh.easyshell.util.postgresql;


import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBColumnField;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;


/**
 * PostgreSQL字段类型工具类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlColumnUtil {

    /**
     * 初始化
     */
    public static void init() {
        // ===== 字符类型 =====
        DBColumnField varcharField = new DBColumnField("VARCHAR");
        varcharField.alias = "CHARACTER VARYING";
        varcharField.suggestSize = 255;
        varcharField.supportSize = true;
        varcharField.supportString = true;
        varcharField.supportDefaultValue = true;

        DBColumnField charField = new DBColumnField("CHAR");
        charField.alias = "CHARACTER";
        charField.suggestSize = 1;
        charField.supportSize = true;
        charField.supportString = true;
        charField.supportDefaultValue = true;

        // 同时保留 PostgreSQL 系统目录返回的原始类型名称
        DBColumnField characterVaryingField = new DBColumnField("CHARACTER VARYING");
        characterVaryingField.alias = "VARCHAR";
        characterVaryingField.suggestSize = 255;
        characterVaryingField.supportSize = true;
        characterVaryingField.supportString = true;
        characterVaryingField.supportDefaultValue = true;

        DBColumnField characterField = new DBColumnField("CHARACTER");
        characterField.alias = "CHAR";
        characterField.suggestSize = 1;
        characterField.supportSize = true;
        characterField.supportString = true;
        characterField.supportDefaultValue = true;

        DBColumnField textField = new DBColumnField("TEXT");
        textField.supportText = true;
        textField.supportDefaultValue = true;

        DBColumnField uuidField = new DBColumnField("UUID");
        uuidField.supportString = true;
        uuidField.supportDefaultValue = true;
        uuidField.exampleValue = "00000000-0000-0000-0000-000000000000";

        DBColumnField jsonField = new DBColumnField("JSON");
        jsonField.supportJson = true;
        jsonField.supportDefaultValue = true;

        DBColumnField jsonbField = new DBColumnField("JSONB");
        jsonbField.supportJson = true;
        jsonbField.supportDefaultValue = true;

        DBColumnField xmlField = new DBColumnField("XML");
        xmlField.supportText = true;
        xmlField.supportDefaultValue = true;

        DBColumnField inetField = new DBColumnField("INET");
        inetField.supportString = true;
        inetField.supportDefaultValue = true;
        inetField.exampleValue = "192.168.0.1";

        DBColumnField cidrField = new DBColumnField("CIDR");
        cidrField.supportString = true;
        cidrField.supportDefaultValue = true;
        cidrField.exampleValue = "192.168.0.0/24";

        DBColumnField macaddrField = new DBColumnField("MACADDR");
        macaddrField.supportString = true;
        macaddrField.supportDefaultValue = true;
        macaddrField.exampleValue = "08:00:2b:01:02:03";

        // ===== 数值类型 =====
        DBColumnField smallintField = new DBColumnField("SMALLINT");
        smallintField.alias = "INT2";
        smallintField.minValue = -32768L;
        smallintField.maxValue = 32767L;
        smallintField.supportInteger = true;
        smallintField.supportDefaultValue = true;
        smallintField.supportAutoIncrement = true;

        DBColumnField integerField = new DBColumnField("INTEGER");
        integerField.alias = "INT4";
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
        bigintField.alias = "INT8";
        bigintField.minValue = Long.MIN_VALUE;
        bigintField.maxValue = Long.MAX_VALUE;
        bigintField.supportInteger = true;
        bigintField.supportDefaultValue = true;
        bigintField.supportAutoIncrement = true;

        // 序列类型自带默认值，无需再设置默认值
        DBColumnField smallserialField = new DBColumnField("SMALLSERIAL");
        smallserialField.alias = "SERIAL2";
        smallserialField.minValue = -32768L;
        smallserialField.maxValue = 32767L;
        smallserialField.supportInteger = true;
        smallserialField.supportAutoIncrement = true;

        DBColumnField serialField = new DBColumnField("SERIAL");
        serialField.alias = "SERIAL4";
        serialField.minValue = (long) Integer.MIN_VALUE;
        serialField.maxValue = (long) Integer.MAX_VALUE;
        serialField.supportInteger = true;
        serialField.supportAutoIncrement = true;

        DBColumnField bigserialField = new DBColumnField("BIGSERIAL");
        bigserialField.alias = "SERIAL8";
        bigserialField.minValue = Long.MIN_VALUE;
        bigserialField.maxValue = Long.MAX_VALUE;
        bigserialField.supportInteger = true;
        bigserialField.supportAutoIncrement = true;

        DBColumnField numericField = new DBColumnField("NUMERIC");
        numericField.alias = "DECIMAL";
        numericField.suggestSize = 20;
        numericField.supportSize = true;
        numericField.supportDigits = true;
        numericField.supportDefaultValue = true;

        DBColumnField realField = new DBColumnField("REAL");
        realField.alias = "FLOAT4";
        realField.supportDigits = true;
        realField.supportDefaultValue = true;

        DBColumnField doublePrecisionField = new DBColumnField("DOUBLE PRECISION");
        doublePrecisionField.alias = "FLOAT8";
        doublePrecisionField.supportDigits = true;
        doublePrecisionField.supportDefaultValue = true;

        DBColumnField moneyField = new DBColumnField("MONEY");
        moneyField.supportDigits = true;
        moneyField.supportDefaultValue = true;
        moneyField.exampleValue = "0.00";

        // ===== 日期时间类型 =====
        DBColumnField dateField = new DBColumnField("DATE");
        dateField.supportDefaultValue = true;

        DBColumnField timeField = new DBColumnField("TIME");
        timeField.alias = "TIME WITHOUT TIME ZONE";
        timeField.supportDefaultValue = true;

        DBColumnField timeWithTzField = new DBColumnField("TIME WITH TIME ZONE");
        timeWithTzField.supportDefaultValue = true;

        // 同时保留 PostgreSQL 系统目录返回的原始类型名称
        DBColumnField timeWithoutTzField = new DBColumnField("TIME WITHOUT TIME ZONE");
        timeWithoutTzField.alias = "TIME";
        timeWithoutTzField.supportDefaultValue = true;

        DBColumnField timestampWithoutTzField = new DBColumnField("TIMESTAMP WITHOUT TIME ZONE");
        timestampWithoutTzField.alias = "TIMESTAMP";
        timestampWithoutTzField.supportTimestamp = true;
        timestampWithoutTzField.supportDefaultValue = true;

        DBColumnField timestampField = new DBColumnField("TIMESTAMP");
        timestampField.alias = "TIMESTAMP WITHOUT TIME ZONE";
        timestampField.supportTimestamp = true;
        timestampField.supportDefaultValue = true;

        DBColumnField timestampWithTzField = new DBColumnField("TIMESTAMP WITH TIME ZONE");
        timestampWithTzField.supportTimestamp = true;
        timestampWithTzField.supportDefaultValue = true;

        DBColumnField intervalField = new DBColumnField("INTERVAL");
        intervalField.supportDefaultValue = true;
        intervalField.exampleValue = "INTERVAL '1 day'";

        // ===== 布尔类型 =====
        DBColumnField booleanField = new DBColumnField("BOOLEAN");
        booleanField.alias = "BOOL";
        booleanField.minValue = 0L;
        booleanField.maxValue = 1L;
        booleanField.supportBoolean = true;
        booleanField.supportDefaultValue = true;
        booleanField.exampleValue = "TRUE";

        // ===== 位串类型 =====
        DBColumnField bitField = new DBColumnField("BIT");
        bitField.suggestSize = 1;
        bitField.supportSize = true;
        bitField.supportBit = true;
        bitField.supportDefaultValue = true;
        bitField.exampleValue = "B'1'";

        DBColumnField bitVaryingField = new DBColumnField("BIT VARYING");
        bitVaryingField.alias = "VARBIT";
        bitVaryingField.suggestSize = 32;
        bitVaryingField.supportSize = true;
        bitVaryingField.supportBit = true;
        bitVaryingField.supportDefaultValue = true;
        bitVaryingField.exampleValue = "B'1010'";

        // ===== 二进制类型 =====
        DBColumnField byteaField = new DBColumnField("BYTEA");
        byteaField.supportBinary = true;
        byteaField.supportDefaultValue = true;
        byteaField.exampleValue = "E'\\\\xDEADBEEF'";

        // ===== 几何类型 =====
        DBColumnField pointField = new DBColumnField("POINT");
        pointField.supportGeometry = true;
        pointField.supportDefaultValue = true;
        pointField.exampleValue = "(1,2)";

        DBColumnField lineField = new DBColumnField("LINE");
        lineField.supportGeometry = true;
        lineField.supportDefaultValue = true;
        lineField.exampleValue = "{1,2,3}";

        DBColumnField lsegField = new DBColumnField("LSEG");
        lsegField.supportGeometry = true;
        lsegField.supportDefaultValue = true;
        lsegField.exampleValue = "[(1,2),(3,4)]";

        DBColumnField boxField = new DBColumnField("BOX");
        boxField.supportGeometry = true;
        boxField.supportDefaultValue = true;
        boxField.exampleValue = "(1,2),(3,4)";

        DBColumnField pathField = new DBColumnField("PATH");
        pathField.supportGeometry = true;
        pathField.supportDefaultValue = true;
        pathField.exampleValue = "[(1,2),(3,4)]";

        DBColumnField polygonField = new DBColumnField("POLYGON");
        polygonField.supportGeometry = true;
        polygonField.supportDefaultValue = true;
        polygonField.exampleValue = "((1,2),(3,4),(5,6))";

        DBColumnField circleField = new DBColumnField("CIRCLE");
        circleField.supportGeometry = true;
        circleField.supportDefaultValue = true;
        circleField.exampleValue = "<(1,2),3>";

        // ===== 全文检索类型 =====
        DBColumnField tsvectorField = new DBColumnField("TSVECTOR");
        tsvectorField.supportText = true;
        tsvectorField.supportDefaultValue = true;

        DBColumnField tsqueryField = new DBColumnField("TSQUERY");
        tsqueryField.supportText = true;
        tsqueryField.supportDefaultValue = true;

        // ===== 注册到列表 =====

        // 字符串
        putFiled(varcharField);
        putFiled(characterVaryingField);
        putFiled(charField);
        putFiled(characterField);
        putFiled(textField);
        putFiled(uuidField);
        putFiled(jsonField);
        putFiled(jsonbField);
        putFiled(xmlField);

        // 数字
        putFiled(smallintField);
        putFiled(integerField);
        putFiled(intField);
        putFiled(bigintField);
        putFiled(smallserialField);
        putFiled(serialField);
        putFiled(bigserialField);
        putFiled(numericField);
        putFiled(realField);
        putFiled(doublePrecisionField);
        putFiled(moneyField);

        // 时间
        putFiled(timeField);
        putFiled(timeWithoutTzField);
        putFiled(dateField);
        putFiled(timestampField);
        putFiled(timestampWithoutTzField);
        putFiled(intervalField);

        // 其他
        putFiled(booleanField);
        putFiled(bitField);
        putFiled(bitVaryingField);

        // 二进制
        putFiled(byteaField);

        // 不常见
        putFiled(timeWithTzField);
        putFiled(timestampWithTzField);
        putFiled(inetField);
        putFiled(cidrField);
        putFiled(macaddrField);
        putFiled(pointField);
        putFiled(lineField);
        putFiled(lsegField);
        putFiled(boxField);
        putFiled(pathField);
        putFiled(polygonField);
        putFiled(circleField);
        putFiled(tsvectorField);
        putFiled(tsqueryField);
    }

    private static void putFiled(DBColumnField columnField) {
        DBColumnFieldManager.putFiled(DBDialect.POSTGRESQL, columnField);
    }

    public static Object defaultValue(String type) {
        if (DBColumnFieldManager.supportDefaultValue(DBDialect.POSTGRESQL, type)) {
            if (DBColumnFieldManager.supportDigits(DBDialect.POSTGRESQL, type)) {
                return 0.0;
            }
            if (DBColumnFieldManager.supportInteger(DBDialect.POSTGRESQL, type)) {
                return 0;
            }
            if (DBColumnFieldManager.supportBoolean(DBDialect.POSTGRESQL, type)) {
                return "TRUE";
            }
            if (DBColumnFieldManager.supportString(DBDialect.POSTGRESQL, type)) {
                return "";
            }
            if (DBColumnFieldManager.supportJson(DBDialect.POSTGRESQL, type)) {
                return "{}";
            }
            if (DBColumnFieldManager.supportBinary(DBDialect.POSTGRESQL, type)) {
                return new byte[]{};
            }
        }
        return null;
    }

    public static boolean isYearType(String type) {
        // PostgreSQL没有YEAR类型
        return false;
    }

    public static boolean isDateType(String type) {
        return StringUtil.equalsIgnoreCase("DATE", type);
    }

    public static boolean isDateTimeType(String type) {
        return StringUtil.equalsAnyIgnoreCase(type,
                "TIMESTAMP",
                "TIMESTAMP WITHOUT TIME ZONE",
                "TIMESTAMP WITH TIME ZONE");
    }

    public static boolean isTimeType(String type) {
        return StringUtil.equalsAnyIgnoreCase(type,
                "TIME",
                "TIME WITHOUT TIME ZONE",
                "TIME WITH TIME ZONE");
    }
}
