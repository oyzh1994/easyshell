package cn.oyzh.easyshell.util.sqlite;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBColumnField;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQLite 字段类型能力工具。
 *
 * @since 2026-11-02
 */
public final class ShellSqliteColumnUtil {

    private ShellSqliteColumnUtil() {
    }

    public static void init() {
        DBColumnField integer = new DBColumnField("INTEGER");
        integer.alias = "INT";
        integer.supportSize = true;
        integer.supportInteger = true;
        integer.supportDefaultValue = true;
        integer.supportAutoIncrement = true;
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, integer);

        DBColumnField real = new DBColumnField("REAL");
        real.alias = "DOUBLE";
        real.supportDigits = true;
        real.supportDefaultValue = true;
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, real);

        DBColumnField numeric = new DBColumnField("NUMERIC");
        numeric.alias = "DECIMAL";
        numeric.supportDigits = true;
        numeric.supportDefaultValue = true;
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, numeric);

        DBColumnField text = new DBColumnField("TEXT");
        text.alias = "VARCHAR";
        text.supportSize = true;
        text.supportText = true;
        text.supportString = true;
        text.supportDefaultValue = true;
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, text);

        DBColumnField blob = new DBColumnField("BLOB");
        blob.alias = "BINARY";
        blob.supportBinary = true;
        blob.supportDefaultValue = true;
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, blob);

        DBColumnField bool = new DBColumnField("BOOLEAN");
        bool.alias = "BOOL";
        bool.supportBoolean = true;
        bool.supportDefaultValue = true;
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, bool);

        DBColumnField date = new DBColumnField("DATE");
        date.supportTimestamp = true;
        date.supportDefaultValue = true;
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, date);

        DBColumnField time = new DBColumnField("TIME");
        time.supportTimestamp = true;
        time.supportDefaultValue = true;
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, time);

        DBColumnField dateTime = new DBColumnField("DATETIME");
        dateTime.alias = "TIMESTAMP";
        dateTime.supportTimestamp = true;
        dateTime.supportDefaultValue = true;
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, dateTime);

        DBColumnField json =new DBColumnField("JSON");
        json.alias = "JSONB";
        json.supportJson = true;
        json.supportText = true;
        json.supportString = true;
        json.supportDefaultValue = true;
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, json);
    }

//    private static DBColumnField field(String name, String alias, boolean supportSize,
//                                       boolean supportString, boolean supportBinary) {
//        DBColumnField field = new DBColumnField(name);
//        field.alias = alias;
//        field.supportSize = supportSize;
//        field.supportString = supportString;
//        field.supportBinary = supportBinary;
//        field.suggestSize = supportSize ? 255 : null;
//        return field;
//    }

    public static Object defaultValue(String type) {
        if (isBooleanType(type)) {
            return false;
        }
        if (isIntegerType(type)) {
            return 0L;
        }
        if (isRealType(type)) {
            return 0.0D;
        }
        if (isTextType(type)) {
            return "";
        }
        return null;
    }

    public static boolean isIntegerType(String type) {
        return contains(type, "INT") || isBooleanType(type);
    }

    public static boolean isRealType(String type) {
        return isFloatingType(type) || isDecimalType(type);
    }

    public static boolean isFloatingType(String type) {
        return contains(type, "REAL") || contains(type, "FLOA") || contains(type, "DOUB");
    }

    public static boolean isDecimalType(String type) {
        return contains(type, "NUM") || contains(type, "DEC");
    }

    public static boolean isTextType(String type) {
        return contains(type, "CHAR") || contains(type, "CLOB") || contains(type, "TEXT")
                || contains(type, "STRING") || isJsonType(type);
    }

    public static boolean isBlobType(String type) {
        return contains(type, "BLOB") || contains(type, "BINARY");
    }

    public static boolean isBooleanType(String type) {
        return contains(type, "BOOL");
    }

    public static boolean isJsonType(String type) {
        return contains(type, "JSON");
    }

    public static boolean isDateType(String type) {
        return contains(type, "DATE") && !isDateTimeType(type);
    }

    public static boolean isDateTimeType(String type) {
        return contains(type, "DATETIME") || contains(type, "TIMESTAMP");
    }

    public static boolean isTimeType(String type) {
        return contains(type, "TIME") && !isDateTimeType(type);
    }

    private static boolean contains(String type, String token) {
        return type != null && type.toUpperCase(java.util.Locale.ROOT).contains(token);
    }

    public static String normalizeType(String type) {
        String value = StringUtil.blankToDefault(type, "TEXT").trim();
        int index = value.indexOf('(');
        return index < 0 ? value : value.substring(0, index).trim();
    }

    public static Integer parseSize(String type) {
        if (StringUtil.isBlank(type)) {
            return null;
        }
        Matcher matcher = Pattern.compile("\\(\\s*(\\d+)").matcher(type);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
