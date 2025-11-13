package cn.oyzh.easyshell.util.mysql;


import cn.oyzh.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2024/1/29
 */
public class ShellMysqlColumnUtil {

    private static final List<ShellMysqlColumnField> COLUMN_FIELD = new ArrayList<>();

    static {

        ShellMysqlColumnField charFiled = new ShellMysqlColumnField("CHAR");
        charFiled.suggestSize = 255;
        charFiled.supportSize = true;
        charFiled.supportString = true;
        charFiled.supportKeySize = true;
        charFiled.supportCharset = true;
        charFiled.supportDefaultValue = true;

        ShellMysqlColumnField varcharField = new ShellMysqlColumnField("VARCHAR");
        varcharField.suggestSize = 255;
        varcharField.supportSize = true;
        varcharField.supportString = true;
        varcharField.supportCharset = true;
        varcharField.supportKeySize = true;
        varcharField.supportDefaultValue = true;

        ShellMysqlColumnField intField = new ShellMysqlColumnField("INT");
        intField.suggestSize = 11;
        intField.supportSize = true;
        intField.supportInteger = true;
        intField.supportUnsigned = true;
        intField.supportZeroFill = true;
        intField.supportDefaultValue = true;
        intField.supportAutoIncrement = true;

        ShellMysqlColumnField bigintFiled = new ShellMysqlColumnField("BIGINT");
        bigintFiled.suggestSize = 20;
        bigintFiled.supportSize = true;
        bigintFiled.supportInteger = true;
        bigintFiled.supportUnsigned = true;
        bigintFiled.supportZeroFill = true;
        bigintFiled.supportDefaultValue = true;
        bigintFiled.supportAutoIncrement = true;

        ShellMysqlColumnField mediumintField = new ShellMysqlColumnField("MEDIUMINT");
        mediumintField.suggestSize = 10;
        mediumintField.supportSize = true;
        mediumintField.supportInteger = true;
        mediumintField.supportUnsigned = true;
        mediumintField.supportZeroFill = true;
        mediumintField.supportDefaultValue = true;
        mediumintField.supportAutoIncrement = true;

        ShellMysqlColumnField tinyintField = new ShellMysqlColumnField("TINYINT");
        tinyintField.suggestSize = 4;
        tinyintField.supportSize = true;
        tinyintField.supportInteger = true;
        tinyintField.supportUnsigned = true;
        tinyintField.supportZeroFill = true;
        tinyintField.supportDefaultValue = true;
        tinyintField.supportAutoIncrement = true;

        ShellMysqlColumnField smallintFiled = new ShellMysqlColumnField("SMALLINT");
        smallintFiled.suggestSize = 6;
        smallintFiled.supportSize = true;
        smallintFiled.supportInteger = true;
        smallintFiled.supportUnsigned = true;
        smallintFiled.supportZeroFill = true;
        smallintFiled.supportDefaultValue = true;
        smallintFiled.supportAutoIncrement = true;

        ShellMysqlColumnField integerField = new ShellMysqlColumnField("INTEGER");
        integerField.suggestSize = 11;
        integerField.supportSize = true;
        integerField.supportInteger = true;
        integerField.supportZeroFill = true;
        integerField.supportDefaultValue = true;
        integerField.supportAutoIncrement = true;

        ShellMysqlColumnField datetimeField = new ShellMysqlColumnField("DATETIME");
        datetimeField.suggestSize = 6;
        datetimeField.supportTimestamp = true;
        datetimeField.supportDefaultValue = true;

        ShellMysqlColumnField timestampField = new ShellMysqlColumnField("TIMESTAMP");
        timestampField.suggestSize = 6;
        timestampField.supportTimestamp = true;
        timestampField.supportDefaultValue = true;

        ShellMysqlColumnField dateField = new ShellMysqlColumnField("DATE");
        dateField.supportDefaultValue = true;

        ShellMysqlColumnField yearField = new ShellMysqlColumnField("YEAR");
        yearField.suggestSize = 4;
        yearField.supportDefaultValue = true;

        ShellMysqlColumnField timeField = new ShellMysqlColumnField("TIME");
        timeField.suggestSize = 6;
        timeField.supportDefaultValue = true;

        ShellMysqlColumnField textField = new ShellMysqlColumnField("TEXT");
        textField.supportCharset = true;
        textField.supportString = true;
        textField.supportKeySize = true;

        ShellMysqlColumnField mediumtextField = new ShellMysqlColumnField("MEDIUMTEXT");
        mediumtextField.supportString = true;
        mediumtextField.supportKeySize = true;
        mediumtextField.supportCharset = true;

        ShellMysqlColumnField longtextField = new ShellMysqlColumnField("LONGTEXT");
        longtextField.supportString = true;
        longtextField.supportKeySize = true;
        longtextField.supportCharset = true;

        ShellMysqlColumnField tinytextFiled = new ShellMysqlColumnField("TINYTEXT");
        tinytextFiled.supportString = true;
        tinytextFiled.supportKeySize = true;
        tinytextFiled.supportCharset = true;

        ShellMysqlColumnField floatField = new ShellMysqlColumnField("FLOAT");
        floatField.suggestSize = 11;
        floatField.supportSize = true;
        floatField.supportDigits = true;
        floatField.supportUnsigned = true;
        floatField.supportZeroFill = true;
        floatField.supportDefaultValue = true;
        floatField.supportAutoIncrement = true;

        ShellMysqlColumnField doubleField = new ShellMysqlColumnField("DOUBLE");
        doubleField.supportSize = true;
        doubleField.suggestSize = 20;
        doubleField.supportDigits = true;
        doubleField.supportUnsigned = true;
        doubleField.supportZeroFill = true;
        doubleField.supportDefaultValue = true;
        doubleField.supportAutoIncrement = true;

        ShellMysqlColumnField decimalField = new ShellMysqlColumnField("DECIMAL");
        decimalField.suggestSize = 20;
        decimalField.supportSize = true;
        decimalField.supportDigits = true;
        decimalField.supportZeroFill = true;
        decimalField.supportUnsigned = true;
        decimalField.supportDefaultValue = true;
        decimalField.supportAutoIncrement = true;

        ShellMysqlColumnField bitFiled = new ShellMysqlColumnField("BIT");
        bitFiled.minValue = 0L;
        bitFiled.maxValue = 1L;
        bitFiled.suggestSize = 1;
        bitFiled.supportBit = true;
        bitFiled.supportSize = true;

        ShellMysqlColumnField jsonField = new ShellMysqlColumnField("JSON");
        // jsonField.supportSize = true;
        jsonField.supportJson = true;

        ShellMysqlColumnField enumField = new ShellMysqlColumnField("ENUM");
        enumField.supportEnum = true;
        enumField.supportValue = true;
        enumField.supportCharset = true;
        enumField.supportDefaultValue = true;

        ShellMysqlColumnField setField = new ShellMysqlColumnField("SET");
        setField.supportEnum = true;
        setField.supportValue = true;
        setField.supportDefaultValue = true;

        ShellMysqlColumnField binaryField = new ShellMysqlColumnField("BINARY");
        binaryField.suggestSize = 255;
        binaryField.supportSize = true;
        binaryField.supportBinary = true;
        binaryField.supportDefaultValue = true;

        ShellMysqlColumnField varbinaryField = new ShellMysqlColumnField("VARBINARY");
        varbinaryField.suggestSize = 65535;
        varbinaryField.supportSize = true;
        varbinaryField.supportBinary = true;
        varbinaryField.supportDefaultValue = true;

        ShellMysqlColumnField blobField = new ShellMysqlColumnField("BLOB");
        blobField.supportBinary = true;
        blobField.supportDefaultValue = true;

        ShellMysqlColumnField longblobField = new ShellMysqlColumnField("LONGBLOB");
        longblobField.supportBinary = true;
        longblobField.supportDefaultValue = true;

        ShellMysqlColumnField tinyblobField = new ShellMysqlColumnField("TINYBLOB");
        tinyblobField.supportBinary = true;
        tinyblobField.supportDefaultValue = true;

        ShellMysqlColumnField mediumblobField = new ShellMysqlColumnField("MEDIUMBLOB");
        mediumblobField.supportBinary = true;
        mediumblobField.supportDefaultValue = true;

        ShellMysqlColumnField geometryField = new ShellMysqlColumnField("GEOMETRY");
        geometryField.exampleValue = "POINT(0 0)";
        geometryField.supportGeometry = true;

        ShellMysqlColumnField pointField = new ShellMysqlColumnField("POINT");
        pointField.exampleValue = "POINT(0 0)";
        pointField.supportGeometry = true;

        ShellMysqlColumnField multipointField = new ShellMysqlColumnField("MULTIPOINT");
        multipointField.exampleValue = "MULTIPOINT((0 0), (1 1))";
        multipointField.supportGeometry = true;

        ShellMysqlColumnField polygonField = new ShellMysqlColumnField("POLYGON");
        polygonField.exampleValue = "POLYGON((0 0,5 0,5 5,0 5,0 0))";
        polygonField.supportGeometry = true;

        ShellMysqlColumnField multipolygonField = new ShellMysqlColumnField("MULTIPOLYGON");
        multipolygonField.exampleValue = "MULTIPOLYGON(((0 0,5 0,5 5,0 5,0 0)), ((0 0,10 0,10 10,0 10,0 0)))";
        multipolygonField.supportGeometry = true;

        ShellMysqlColumnField linestringField = new ShellMysqlColumnField("LINESTRING");
        linestringField.exampleValue = "LINESTRING(0 0,1 1,2 2)";
        linestringField.supportGeometry = true;

        ShellMysqlColumnField multilinestringField = new ShellMysqlColumnField("MULTILINESTRING");
        multilinestringField.exampleValue = "MULTILINESTRING((0 0,1 1,2 2), (3 3,4 4,5 5))";
        multilinestringField.supportGeometry = true;

        ShellMysqlColumnField geometrycollectionField = new ShellMysqlColumnField("GEOMETRYCOLLECTION");
        geometrycollectionField.exampleValue = "GEOMETRYCOLLECTION(POINT(0 0),LINESTRING(0 0,1 1,2 2),POLYGON((5 5, 6 5, 6 6, 5 6, 5 5)))";
        geometrycollectionField.supportGeometry = true;

        putFiled(charFiled);
        putFiled(varcharField);

        putFiled(intField);
        putFiled(bigintFiled);
        putFiled(tinyintField);
        putFiled(smallintFiled);
        putFiled(mediumintField);
        putFiled(integerField);

        putFiled(floatField);
        putFiled(doubleField);
        putFiled(decimalField);

        putFiled(datetimeField);
        putFiled(timestampField);
        putFiled(dateField);
        putFiled(yearField);
        putFiled(timeField);

        putFiled(textField);
        putFiled(longtextField);
        putFiled(tinytextFiled);
        putFiled(mediumtextField);

        putFiled(bitFiled);
        putFiled(jsonField);

        putFiled(enumField);
        putFiled(setField);

        putFiled(binaryField);
        putFiled(varbinaryField);
        putFiled(blobField);
        putFiled(longblobField);
        putFiled(mediumblobField);
        putFiled(tinyblobField);

        putFiled(geometryField);
        putFiled(pointField);
        putFiled(multipointField);
        putFiled(polygonField);
        putFiled(multipolygonField);
        putFiled(linestringField);
        putFiled(multilinestringField);
        putFiled(geometrycollectionField);
    }

    private static void putFiled(ShellMysqlColumnField columnField) {
        COLUMN_FIELD.add(columnField);
    }

    public static List<String> fields() {
        return COLUMN_FIELD.parallelStream().map(ShellMysqlColumnField::getName).collect(Collectors.toList());
    }

    public static boolean supportSize(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportSize;
            }
        }
        return false;
    }

    public static Integer suggestSize(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.suggestSize;
            }
        }
        return null;
    }

    public static boolean supportUnsigned(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportUnsigned;
            }
        }
        return false;
    }

    public static boolean supportJson(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportJson;
            }
        }
        return false;
    }

    public static boolean supportKeySize(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportKeySize;
            }
        }
        return false;
    }

    public static boolean supportString(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportString;
            }
        }
        return false;
    }

    public static boolean supportValue(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportValue;
            }
        }
        return false;
    }

    public static boolean supportZeroFill(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportZeroFill;
            }
        }
        return false;
    }

    public static boolean supportBit(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportBit;
            }
        }
        return false;
    }

    public static boolean supportBinary(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportBinary;
            }
        }
        return false;
    }

    public static boolean supportDigits(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportDigits;
            }
        }
        return false;
    }

    public static boolean supportDefaultValue(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportDefaultValue;
            }
        }
        return false;
    }

    public static boolean supportGeometry(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportGeometry;
            }
        }
        return false;
    }

    public static boolean supportEnum(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportEnum;
            }
        }
        return false;
    }

    public static boolean supportCharset(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportCharset;
            }
        }
        return false;
    }

    public static boolean supportTimestamp(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportTimestamp;
            }
        }
        return false;
    }

    public static boolean supportInteger(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportInteger;
            }
        }
        return false;
    }

    public static boolean supportAutoIncrement(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportAutoIncrement;
            }
        }
        return false;
    }

    public static Object exampleValue(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.exampleValue;
            }
        }
        return false;
    }

    public static Long minValue(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.minValue;
            }
        }
        return null;
    }

    public static Long maxValue(String type) {
        for (ShellMysqlColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.maxValue;
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

    public static boolean isTimeType(String type) {
        return "TIME".equalsIgnoreCase(type);
    }

    public static boolean isPolygonType(String type) {
        return "POLYGON".equalsIgnoreCase(type);
    }

    public static boolean isMultiPolygonType(String type) {
        return "MULTIPOLYGON".equalsIgnoreCase(type);
    }

    public static boolean isPointType(String type) {
        return "Point".equalsIgnoreCase(type);
    }

    public static boolean isMultiPointType(String type) {
        return "MultiPoint".equalsIgnoreCase(type);
    }

    public static boolean isLineStringType(String type) {
        return "LineString".equalsIgnoreCase(type);
    }

    public static boolean isMultiLineStringType(String type) {
        return "MultiLineString".equalsIgnoreCase(type);
    }

    public static boolean isGeomCollectionType(String type) {
        return "GeomCollection".equalsIgnoreCase(type);
    }

    public static boolean isGeometryType(String type) {
        return "Geometry".equalsIgnoreCase(type);
    }

    public static Object defaultValue(String type) {
        if (supportDefaultValue(type)) {
            if (supportDigits(type)) {
                return 0.0;
            }
            if (supportInteger(type)) {
                return 0;
            }
            if (supportString(type)) {
                return "";
            }
            if (supportJson(type)) {
                return "{'a':1}";
            }
            if (supportBinary(type)) {
                return new byte[]{};
            }
        }
        return null;
    }
}
