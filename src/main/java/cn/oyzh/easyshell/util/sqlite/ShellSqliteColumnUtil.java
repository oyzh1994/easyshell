package cn.oyzh.easyshell.util.sqlite;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBColumnField;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;

import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQLite 字段类型能力工具。
 *
 * @since 2026-11-02
 */
public final class ShellSqliteColumnUtil {

    private static boolean initialized;

    private ShellSqliteColumnUtil() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        DBColumnField integer = new DBColumnField("INTEGER");
        integer.alias = "INT";
        integer.supportSize = true;
        integer.supportInteger = true;
        integer.supportDefaultValue = true;
        integer.supportAutoIncrement = true;

        DBColumnField real = new DBColumnField("REAL");
        real.alias = "DOUBLE";
        real.supportDigits = true;
        real.supportDefaultValue = true;

        DBColumnField numeric = new DBColumnField("NUMERIC");
        numeric.alias = "DECIMAL";
        numeric.supportDigits = true;
        numeric.supportDefaultValue = true;

        DBColumnField text = new DBColumnField("TEXT");
        text.alias = "VARCHAR";
        text.supportSize = true;
        text.supportText = true;
        text.supportString = true;
        text.supportDefaultValue = true;

        DBColumnField blob = new DBColumnField("BLOB");
        blob.alias = "BINARY";
        blob.supportBinary = true;
        blob.supportDefaultValue = true;

        DBColumnField any = new DBColumnField("ANY");
        any.supportJson = true;
        any.supportText = true;
        any.supportString = true;
        any.supportDefaultValue = true;

        DBColumnField bool = new DBColumnField("BOOLEAN");
        bool.alias = "BOOL";
        bool.supportBoolean = true;
        bool.supportDefaultValue = true;

        DBColumnField date = new DBColumnField("DATE");
        date.supportTimestamp = true;
        date.supportDefaultValue = true;

        DBColumnField time = new DBColumnField("TIME");
        time.supportTimestamp = true;
        time.supportDefaultValue = true;

        DBColumnField dateTime = new DBColumnField("DATETIME");
        dateTime.alias = "TIMESTAMP";
        dateTime.supportTimestamp = true;
        dateTime.supportDefaultValue = true;

        DBColumnField json = new DBColumnField("JSON");
        json.alias = "JSONB";
        json.supportJson = true;
        json.supportText = true;
        json.supportString = true;
        json.supportDefaultValue = true;

        // 常用类型优先
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, integer);
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, text);
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, real);
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, blob);
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, numeric);
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, bool);
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, dateTime);
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, date);
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, time);
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, json);

        // 低频和兼容类型
        DBColumnFieldManager.putFiled(DBDialect.SQLITE, any);
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

    /**
     * 将 JDBC 返回类型映射为 SQLite 支持的基本类型。
     *
     * @param typeName JDBC 类型名称
     * @return SQLite 类型
     */
    public static String normalizeJdbcType(String typeName) {
        String type = StringUtil.isBlank(typeName) ? "" : normalizeType(typeName);
        if (isJsonType(type)) {
            return "JSON";
        }
        if (isDateTimeType(type)) {
            return "DATETIME";
        }
        if (isDateType(type)) {
            return "DATE";
        }
        if (isTimeType(type)) {
            return "TIME";
        }
        if (isBooleanType(type)) {
            return "BOOLEAN";
        }
        if (isTextType(type)) {
            return "TEXT";
        }
        if (isBlobType(type)) {
            return "BLOB";
        }
        if (isIntegerType(type)) {
            return "INTEGER";
        }
        if (isFloatingType(type)) {
            return "REAL";
        }
        if (isDecimalType(type)) {
            return "NUMERIC";
        }
        return type;
    }

    /**
     * 将 JDBC 返回类型映射为 SQLite 支持的基本类型。
     *
     * @param jdbcType JDBC 类型
     * @return SQLite 类型
     */
    public static String normalizeJdbcType(int jdbcType) {
        return switch (jdbcType) {
            case Types.BIT, Types.BOOLEAN -> "BOOLEAN";
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT -> "INTEGER";
            case Types.FLOAT, Types.REAL, Types.DOUBLE -> "REAL";
            case Types.NUMERIC, Types.DECIMAL -> "NUMERIC";
            case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.NCHAR, Types.NVARCHAR,
                 Types.LONGNVARCHAR, Types.CLOB, Types.NCLOB, Types.SQLXML -> "TEXT";
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> "BLOB";
            case Types.DATE -> "DATE";
            case Types.TIME, Types.TIME_WITH_TIMEZONE -> "TIME";
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> "DATETIME";
            default -> "ANY";
        };
    }

    /**
     * 获取 JDBC 类型名称中的有效长度。
     *
     * @param typeName JDBC 类型名称
     * @return 长度，无法确定时返回空
     */
    public static Integer normalizeJdbcSize(String typeName) {
        return parseSize(typeName);
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
