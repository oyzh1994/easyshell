package cn.oyzh.easyshell.util.h2;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBColumnField;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;

import java.sql.Types;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * H2 字段类型能力工具。
 *
 * <p>H2 是强类型数据库，类型能力必须按真实类型登记，不能沿用 SQLite 的弱类型规则。</p>
 *
 * @since 2026-11-02
 */
public final class ShellH2ColumnUtil {

    private static final Set<String> INTEGER_TYPES = Set.of(
            "TINYINT", "SMALLINT", "INTEGER", "INT", "INT4", "BIGINT", "INT8",
            "MEDIUMINT", "SERIAL", "BIGSERIAL");

    private static final Set<String> TEXT_TYPES = Set.of(
            "CHARACTER VARYING", "VARCHAR", "VARCHAR2", "VARCHAR_IGNORECASE",
            "CHARACTER", "CHAR", "NCHAR", "NVARCHAR", "CLOB",
            "CHARACTER LARGE OBJECT", "TEXT");

    private static final Set<String> BINARY_TYPES = Set.of(
            "BINARY", "BINARY VARYING", "VARBINARY", "BINARY LARGE OBJECT", "BLOB");

    private static final Set<String> BOOLEAN_TYPES = Set.of("BOOLEAN", "BOOL");

    private static final Set<String> FLOATING_TYPES = Set.of(
            "REAL", "FLOAT", "FLOAT4", "FLOAT8", "DOUBLE", "DOUBLE PRECISION");

    private static final Set<String> DECIMAL_TYPES = Set.of(
            "NUMERIC", "DECIMAL", "DEC", "DECFLOAT");

    private static final Set<String> DATE_TYPES = Set.of("DATE");

    private static final Set<String> TIME_TYPES = Set.of(
            "TIME", "TIME WITHOUT TIME ZONE", "TIME WITH TIME ZONE");

    private static final Set<String> DATETIME_TYPES = Set.of(
            "TIMESTAMP", "DATETIME", "TIMESTAMP WITHOUT TIME ZONE", "TIMESTAMP WITH TIME ZONE");

    private static final Set<String> JSON_TYPES = Set.of("JSON");

    private static final Set<String> GEOMETRY_TYPES = Set.of("GEOMETRY");

    private static final Pattern SIZE_PATTERN = Pattern.compile("\\(\\s*(\\d+)");

    private static final Pattern DIGITS_PATTERN = Pattern.compile("\\(\\s*\\d+\\s*,\\s*(\\d+)");

    private static boolean initialized;

    private ShellH2ColumnUtil() {
    }

    /**
     * 注册 H2 原生类型及常用标准别名。
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        DBColumnField tinyint = integerType("TINYINT", -128L, 127L);
        DBColumnField smallint = integerType("SMALLINT", -32768L, 32767L);
        DBColumnField integer = integerType("INTEGER", (long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE);
        integer.alias = "INT";
        DBColumnField bigint = integerType("BIGINT", Long.MIN_VALUE, Long.MAX_VALUE);
        bigint.supportBigInteger = true;

        DBColumnField characterVarying = stringType("CHARACTER VARYING", "VARCHAR", 255);
        DBColumnField varcharIgnoreCase = stringType("VARCHAR_IGNORECASE", null, 255);
        DBColumnField character = stringType("CHARACTER", "CHAR", 1);
        DBColumnField clob = textType("CHARACTER LARGE OBJECT");
        clob.alias = "CLOB";

        DBColumnField binaryVarying = binaryType("BINARY VARYING", "VARBINARY", 255);
        DBColumnField binary = binaryType("BINARY", null, 1);
        DBColumnField blob = binaryType("BINARY LARGE OBJECT", "BLOB", null);

        DBColumnField numeric = numericType("NUMERIC", "DECIMAL", 20, true);
        DBColumnField decfloat = numericType("DECFLOAT", null, 34, true);
        DBColumnField real = realType("REAL", null);
        DBColumnField doublePrecision = realType("DOUBLE PRECISION", "DOUBLE");

        DBColumnField bool = new DBColumnField("BOOLEAN");
        bool.alias = "BOOL";
        bool.supportBoolean = true;
        bool.supportDefaultValue = true;
        bool.exampleValue = "TRUE";

        DBColumnField date = temporalType("DATE", false);
        DBColumnField time = temporalType("TIME", false);
        DBColumnField timeWithZone = temporalType("TIME WITH TIME ZONE", false);
        DBColumnField timestamp = temporalType("TIMESTAMP", true);
        timestamp.alias = "DATETIME";
        DBColumnField timestampWithZone = temporalType("TIMESTAMP WITH TIME ZONE", true);

        DBColumnField json = new DBColumnField("JSON");
        json.supportJson = true;
        json.supportDefaultValue = true;
        json.exampleValue = "{}";

        DBColumnField uuid = new DBColumnField("UUID");
        uuid.supportString = true;
        uuid.supportDefaultValue = true;
        uuid.exampleValue = "00000000-0000-0000-0000-000000000000";

        DBColumnField geometry = new DBColumnField("GEOMETRY");
        geometry.supportGeometry = true;
        geometry.supportDefaultValue = true;
        geometry.exampleValue = "POINT (0 0)";

        DBColumnField enumeration = new DBColumnField("ENUM");
        enumeration.supportString = true;
        enumeration.supportDefaultValue = true;

        DBColumnField array = new DBColumnField("ARRAY");
        DBColumnField interval = new DBColumnField("INTERVAL");

        // 常用类型优先
        putFiled(integer);
        putFiled(bigint);
        putFiled(characterVarying);
        putFiled(clob);
        putFiled(numeric);
        putFiled(bool);
        putFiled(timestamp);
        putFiled(date);
        putFiled(json);
        putFiled(uuid);

        // 其他 H2 原生类型和常用别名
        putFiled(tinyint);
        putFiled(smallint);
        putFiled(character);
        putFiled(varcharIgnoreCase);
        putFiled(binaryVarying);
        putFiled(binary);
        putFiled(blob);
        putFiled(decfloat);
        putFiled(real);
        putFiled(doublePrecision);
        putFiled(time);
        putFiled(timeWithZone);
        putFiled(timestampWithZone);
        putFiled(geometry);
        putFiled(enumeration);

        // 复合和区间类型在通用文本编辑器中展示
        putFiled(array);
        putFiled(interval);
    }

    private static DBColumnField integerType(String name, Long min, Long max) {
        DBColumnField field = new DBColumnField(name);
        field.minValue = min;
        field.maxValue = max;
        field.supportInteger = true;
        field.supportDefaultValue = true;
        field.supportAutoIncrement = true;
        return field;
    }

    private static DBColumnField stringType(String name, String alias, Integer suggestSize) {
        DBColumnField field = new DBColumnField(name);
        field.alias = alias;
        field.suggestSize = suggestSize;
        field.supportSize = true;
        field.supportString = true;
        field.supportDefaultValue = true;
        return field;
    }

    private static DBColumnField textType(String name) {
        DBColumnField field = new DBColumnField(name);
        field.supportText = true;
        field.supportString = true;
        field.supportDefaultValue = true;
        return field;
    }

    private static DBColumnField binaryType(String name, String alias, Integer suggestSize) {
        DBColumnField field = new DBColumnField(name);
        field.alias = alias;
        field.suggestSize = suggestSize;
        field.supportSize = suggestSize != null;
        field.supportBinary = true;
        field.supportDefaultValue = true;
        return field;
    }

    private static DBColumnField numericType(String name, String alias, Integer suggestSize,
                                             boolean supportDigits) {
        DBColumnField field = new DBColumnField(name);
        field.alias = alias;
        field.suggestSize = suggestSize;
        field.supportSize = true;
        field.supportDigits = supportDigits;
        field.supportDefaultValue = true;
        return field;
    }

    private static DBColumnField realType(String name, String alias) {
        DBColumnField field = new DBColumnField(name);
        field.alias = alias;
        field.supportDigits = true;
        field.supportDefaultValue = true;
        return field;
    }

    private static DBColumnField temporalType(String name, boolean dateTime) {
        DBColumnField field = new DBColumnField(name);
        field.supportTimestamp = dateTime;
        field.supportDefaultValue = true;
        return field;
    }

    private static void putFiled(DBColumnField field) {
        DBColumnFieldManager.putFiled(DBDialect.H2, field);
    }

    public static Object defaultValue(String type) {
        String normalized = normalizeType(type);
        if (isBooleanType(normalized)) {
            return false;
        }
        if (isIntegerType(normalized)) {
            return 0L;
        }
        if (isRealType(normalized)) {
            return 0.0D;
        }
        if (isJsonType(normalized)) {
            return "{}";
        }
        if (isTextType(normalized) || "UUID".equals(normalized) || "ENUM".equals(normalized)) {
            return "";
        }
        return null;
    }

    public static boolean isIntegerType(String type) {
        return INTEGER_TYPES.contains(normalizeType(type));
    }

    public static boolean isRealType(String type) {
        String normalized = normalizeType(type);
        return FLOATING_TYPES.contains(normalized) || DECIMAL_TYPES.contains(normalized);
    }

    public static boolean isFloatingType(String type) {
        return FLOATING_TYPES.contains(normalizeType(type));
    }

    public static boolean isDecimalType(String type) {
        return DECIMAL_TYPES.contains(normalizeType(type));
    }

    public static boolean isTextType(String type) {
        return TEXT_TYPES.contains(normalizeType(type));
    }

    public static boolean isBlobType(String type) {
        return BINARY_TYPES.contains(normalizeType(type));
    }

    public static boolean isBooleanType(String type) {
        return BOOLEAN_TYPES.contains(normalizeType(type));
    }

    public static boolean isJsonType(String type) {
        return JSON_TYPES.contains(normalizeType(type));
    }

    public static boolean isGeometryType(String type) {
        return GEOMETRY_TYPES.contains(normalizeType(type));
    }

    public static boolean isDateType(String type) {
        return DATE_TYPES.contains(normalizeType(type));
    }

    public static boolean isDateTimeType(String type) {
        return DATETIME_TYPES.contains(normalizeType(type));
    }

    public static boolean isTimeType(String type) {
        return TIME_TYPES.contains(normalizeType(type));
    }

    /**
     * 规范化类型名称，移除长度、精度参数并统一大小写。
     *
     * @param type 类型
     * @return 规范化类型
     */
    public static String normalizeType(String type) {
        String value = StringUtil.blankToDefault(type, "CHARACTER VARYING").trim();
        int index = value.indexOf('(');
        if (index >= 0) {
            value = value.substring(0, index);
        }
        return value.replaceAll("\\s+", " ").trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 将 JDBC 返回类型映射为 H2 标准类型名。
     *
     * @param typeName JDBC 类型名称
     * @return H2 类型
     */
    public static String normalizeJdbcType(String typeName) {
        String type = normalizeType(typeName);
        if (isJsonType(type)) {
            return "JSON";
        }
        if (isDateTimeType(type)) {
            return "TIMESTAMP";
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
            return "CHARACTER VARYING";
        }
        if (isBlobType(type)) {
            return "BINARY LARGE OBJECT";
        }
        if (isIntegerType(type)) {
            return "INTEGER";
        }
        if (isFloatingType(type)) {
            return "DOUBLE PRECISION";
        }
        if (isDecimalType(type)) {
            return "NUMERIC";
        }
        return type;
    }

    /**
     * 将 JDBC 返回类型映射为 H2 标准类型名。
     *
     * @param jdbcType JDBC 类型
     * @return H2 类型
     */
    public static String normalizeJdbcType(int jdbcType) {
        return switch (jdbcType) {
            case Types.BIT, Types.BOOLEAN -> "BOOLEAN";
            case Types.TINYINT -> "TINYINT";
            case Types.SMALLINT -> "SMALLINT";
            case Types.INTEGER -> "INTEGER";
            case Types.BIGINT -> "BIGINT";
            case Types.FLOAT, Types.REAL, Types.DOUBLE -> "DOUBLE PRECISION";
            case Types.NUMERIC, Types.DECIMAL -> "NUMERIC";
            case Types.CHAR -> "CHARACTER";
            case Types.VARCHAR, Types.LONGVARCHAR, Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR ->
                    "CHARACTER VARYING";
            case Types.CLOB, Types.NCLOB, Types.SQLXML -> "CHARACTER LARGE OBJECT";
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> "BINARY LARGE OBJECT";
            case Types.DATE -> "DATE";
            case Types.TIME -> "TIME";
            case Types.TIME_WITH_TIMEZONE -> "TIME WITH TIME ZONE";
            case Types.TIMESTAMP -> "TIMESTAMP";
            case Types.TIMESTAMP_WITH_TIMEZONE -> "TIMESTAMP WITH TIME ZONE";
            case Types.ARRAY -> "ARRAY";
            default -> "CHARACTER VARYING";
        };
    }

    public static Integer normalizeJdbcSize(String typeName) {
        return parseSize(typeName);
    }

    public static Integer parseSize(String type) {
        return parse(type, SIZE_PATTERN);
    }

    public static Integer parseDigits(String type) {
        return parse(type, DIGITS_PATTERN);
    }

    private static Integer parse(String type, Pattern pattern) {
        if (StringUtil.isBlank(type)) {
            return null;
        }
        Matcher matcher = pattern.matcher(type);
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
