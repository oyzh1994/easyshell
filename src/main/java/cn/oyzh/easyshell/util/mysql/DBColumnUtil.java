package cn.oyzh.easyshell.util.mysql;


import cn.oyzh.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2024/1/29
 */
public class DBColumnUtil {

    private static final List<DBColumnField> COLUMN_FIELD = new ArrayList<>();

    static {

        DBColumnField charFiled = new DBColumnField("CHAR");
        charFiled.suggestSize = 255;
        charFiled.supportSize = true;
        charFiled.supportString = true;
        charFiled.supportKeySize = true;
        charFiled.supportCharset = true;
        charFiled.supportDefaultValue = true;

        DBColumnField varcharField = new DBColumnField("VARCHAR");
        varcharField.suggestSize = 255;
        varcharField.supportSize = true;
        varcharField.supportString = true;
        varcharField.supportCharset = true;
        varcharField.supportKeySize = true;
        varcharField.supportDefaultValue = true;

        DBColumnField intField = new DBColumnField("INT");
        intField.suggestSize = 11;
        intField.supportSize = true;
        intField.supportInteger = true;
        intField.supportUnsigned = true;
        intField.supportZeroFill = true;
        intField.supportDefaultValue = true;
        intField.supportAutoIncrement = true;

        DBColumnField bigintFiled = new DBColumnField("BIGINT");
        bigintFiled.suggestSize = 20;
        bigintFiled.supportSize = true;
        bigintFiled.supportInteger = true;
        bigintFiled.supportUnsigned = true;
        bigintFiled.supportZeroFill = true;
        bigintFiled.supportDefaultValue = true;
        bigintFiled.supportAutoIncrement = true;

        DBColumnField mediumintField = new DBColumnField("MEDIUMINT");
        mediumintField.suggestSize = 10;
        mediumintField.supportSize = true;
        mediumintField.supportInteger = true;
        mediumintField.supportUnsigned = true;
        mediumintField.supportZeroFill = true;
        mediumintField.supportDefaultValue = true;
        mediumintField.supportAutoIncrement = true;

        DBColumnField tinyintField = new DBColumnField("TINYINT");
        tinyintField.suggestSize = 4;
        tinyintField.supportSize = true;
        tinyintField.supportInteger = true;
        tinyintField.supportUnsigned = true;
        tinyintField.supportZeroFill = true;
        tinyintField.supportDefaultValue = true;
        tinyintField.supportAutoIncrement = true;

        DBColumnField smallintFiled = new DBColumnField("SMALLINT");
        smallintFiled.suggestSize = 6;
        smallintFiled.supportSize = true;
        smallintFiled.supportInteger = true;
        smallintFiled.supportUnsigned = true;
        smallintFiled.supportZeroFill = true;
        smallintFiled.supportDefaultValue = true;
        smallintFiled.supportAutoIncrement = true;

        DBColumnField integerField = new DBColumnField("INTEGER");
        integerField.suggestSize = 11;
        integerField.supportSize = true;
        integerField.supportInteger = true;
        integerField.supportZeroFill = true;
        integerField.supportDefaultValue = true;
        integerField.supportAutoIncrement = true;

        DBColumnField datetimeField = new DBColumnField("DATETIME");
        datetimeField.suggestSize = 6;
        datetimeField.supportTimestamp = true;
        datetimeField.supportDefaultValue = true;

        DBColumnField timestampField = new DBColumnField("TIMESTAMP");
        timestampField.suggestSize = 6;
        timestampField.supportTimestamp = true;
        timestampField.supportDefaultValue = true;

        DBColumnField dateField = new DBColumnField("DATE");
        dateField.supportDefaultValue = true;

        DBColumnField yearField = new DBColumnField("YEAR");
        yearField.suggestSize = 4;
        yearField.supportDefaultValue = true;

        DBColumnField timeField = new DBColumnField("TIME");
        timeField.suggestSize = 6;
        timeField.supportDefaultValue = true;

        DBColumnField textField = new DBColumnField("TEXT");
        textField.supportCharset = true;
        textField.supportString = true;
        textField.supportKeySize = true;

        DBColumnField mediumtextField = new DBColumnField("MEDIUMTEXT");
        mediumtextField.supportString = true;
        mediumtextField.supportKeySize = true;
        mediumtextField.supportCharset = true;

        DBColumnField longtextField = new DBColumnField("LONGTEXT");
        longtextField.supportString = true;
        longtextField.supportKeySize = true;
        longtextField.supportCharset = true;

        DBColumnField tinytextFiled = new DBColumnField("TINYTEXT");
        tinytextFiled.supportString = true;
        tinytextFiled.supportKeySize = true;
        tinytextFiled.supportCharset = true;

        DBColumnField floatField = new DBColumnField("FLOAT");
        floatField.suggestSize = 11;
        floatField.supportSize = true;
        floatField.supportDigits = true;
        floatField.supportUnsigned = true;
        floatField.supportZeroFill = true;
        floatField.supportDefaultValue = true;
        floatField.supportAutoIncrement = true;

        DBColumnField doubleField = new DBColumnField("DOUBLE");
        doubleField.supportSize = true;
        doubleField.suggestSize = 20;
        doubleField.supportDigits = true;
        doubleField.supportUnsigned = true;
        doubleField.supportZeroFill = true;
        doubleField.supportDefaultValue = true;
        doubleField.supportAutoIncrement = true;

        DBColumnField decimalField = new DBColumnField("DECIMAL");
        decimalField.suggestSize = 20;
        decimalField.supportSize = true;
        decimalField.supportDigits = true;
        decimalField.supportZeroFill = true;
        decimalField.supportUnsigned = true;
        decimalField.supportDefaultValue = true;
        decimalField.supportAutoIncrement = true;

        DBColumnField bitFiled = new DBColumnField("BIT");
        bitFiled.minValue = 0L;
        bitFiled.maxValue = 1L;
        bitFiled.suggestSize = 1;
        bitFiled.supportBit = true;
        bitFiled.supportSize = true;

        DBColumnField jsonField = new DBColumnField("JSON");
        jsonField.supportSize = true;
        jsonField.supportJson = true;

        DBColumnField enumField = new DBColumnField("ENUM");
        enumField.supportEnum = true;
        enumField.supportValue = true;
        enumField.supportDefaultValue = true;

        DBColumnField setField = new DBColumnField("SET");
        setField.supportEnum = true;
        setField.supportValue = true;
        setField.supportDefaultValue = true;

        DBColumnField binaryField = new DBColumnField("BINARY");
        binaryField.suggestSize = 255;
        binaryField.supportBinary = true;
        binaryField.supportDefaultValue = true;

        DBColumnField varbinaryField = new DBColumnField("VARBINARY");
        varbinaryField.suggestSize = 65535;
        varbinaryField.supportBinary = true;
        varbinaryField.supportDefaultValue = true;

        DBColumnField blobField = new DBColumnField("BLOB");
        blobField.supportBinary = true;
        blobField.supportDefaultValue = true;

        DBColumnField longblobField = new DBColumnField("LONGBLOB");
        longblobField.supportBinary = true;
        longblobField.supportDefaultValue = true;

        DBColumnField tinyblobField = new DBColumnField("TINYBLOB");
        tinyblobField.supportBinary = true;
        tinyblobField.supportDefaultValue = true;

        DBColumnField mediumblobField = new DBColumnField("MEDIUMBLOB");
        mediumblobField.supportBinary = true;
        mediumblobField.supportDefaultValue = true;

        DBColumnField geometryField = new DBColumnField("GEOMETRY");
        geometryField.exampleValue = "POINT(0 0)";
        geometryField.supportGeometry = true;

        DBColumnField pointField = new DBColumnField("POINT");
        pointField.exampleValue = "POINT(0 0)";
        pointField.supportGeometry = true;

        DBColumnField multipointField = new DBColumnField("MULTIPOINT");
        multipointField.exampleValue = "MULTIPOINT((0 0), (1 1))";
        multipointField.supportGeometry = true;

        DBColumnField polygonField = new DBColumnField("POLYGON");
        polygonField.exampleValue = "POLYGON((0 0,5 0,5 5,0 5,0 0))";
        polygonField.supportGeometry = true;

        DBColumnField multipolygonField = new DBColumnField("MULTIPOLYGON");
        multipolygonField.exampleValue = "MULTIPOLYGON(((0 0,5 0,5 5,0 5,0 0)), ((0 0,10 0,10 10,0 10,0 0)))";
        multipolygonField.supportGeometry = true;

        DBColumnField linestringField = new DBColumnField("LINESTRING");
        linestringField.exampleValue = "LINESTRING(0 0,1 1,2 2)";
        linestringField.supportGeometry = true;

        DBColumnField multilinestringField = new DBColumnField("MULTILINESTRING");
        multilinestringField.exampleValue = "MULTILINESTRING((0 0,1 1,2 2), (3 3,4 4,5 5))";
        multilinestringField.supportGeometry = true;

        DBColumnField geometrycollectionField = new DBColumnField("GEOMETRYCOLLECTION");
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

    private static void putFiled(DBColumnField columnField) {
        COLUMN_FIELD.add(columnField);
    }

    public static List<String> fields() {
        return COLUMN_FIELD.parallelStream().map(DBColumnField::getName).collect(Collectors.toList());
    }

    private static class DBColumnField {

        private String name;

        private Long maxValue;

        private Long minValue;

        /**
         * 推荐字段长
         */
        private Integer suggestSize;

        private boolean supportBit;

        private String exampleValue;

        private boolean supportSize;

        private boolean supportJson;

        private boolean supportEnum;

        private boolean supportValue;

        private boolean supportBinary;

        private boolean supportDigits;

        private boolean supportString;

        private boolean supportKeySize;

        private boolean supportInteger;

        private boolean supportCharset;

        private boolean supportUnsigned;

        private boolean supportZeroFill;

        private boolean supportGeometry;

        private boolean supportTimestamp;

        private boolean supportDefaultValue;

        private boolean supportAutoIncrement;

        public DBColumnField(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(Long maxValue) {
            this.maxValue = maxValue;
        }

        public Long getMinValue() {
            return minValue;
        }

        public void setMinValue(Long minValue) {
            this.minValue = minValue;
        }

        public Integer getSuggestSize() {
            return suggestSize;
        }

        public void setSuggestSize(Integer suggestSize) {
            this.suggestSize = suggestSize;
        }

        public boolean isSupportBit() {
            return supportBit;
        }

        public void setSupportBit(boolean supportBit) {
            this.supportBit = supportBit;
        }

        public String getExampleValue() {
            return exampleValue;
        }

        public void setExampleValue(String exampleValue) {
            this.exampleValue = exampleValue;
        }

        public boolean isSupportSize() {
            return supportSize;
        }

        public void setSupportSize(boolean supportSize) {
            this.supportSize = supportSize;
        }

        public boolean isSupportJson() {
            return supportJson;
        }

        public void setSupportJson(boolean supportJson) {
            this.supportJson = supportJson;
        }

        public boolean isSupportEnum() {
            return supportEnum;
        }

        public void setSupportEnum(boolean supportEnum) {
            this.supportEnum = supportEnum;
        }

        public boolean isSupportValue() {
            return supportValue;
        }

        public void setSupportValue(boolean supportValue) {
            this.supportValue = supportValue;
        }

        public boolean isSupportBinary() {
            return supportBinary;
        }

        public void setSupportBinary(boolean supportBinary) {
            this.supportBinary = supportBinary;
        }

        public boolean isSupportDigits() {
            return supportDigits;
        }

        public void setSupportDigits(boolean supportDigits) {
            this.supportDigits = supportDigits;
        }

        public boolean isSupportString() {
            return supportString;
        }

        public void setSupportString(boolean supportString) {
            this.supportString = supportString;
        }

        public boolean isSupportKeySize() {
            return supportKeySize;
        }

        public void setSupportKeySize(boolean supportKeySize) {
            this.supportKeySize = supportKeySize;
        }

        public boolean isSupportInteger() {
            return supportInteger;
        }

        public void setSupportInteger(boolean supportInteger) {
            this.supportInteger = supportInteger;
        }

        public boolean isSupportCharset() {
            return supportCharset;
        }

        public void setSupportCharset(boolean supportCharset) {
            this.supportCharset = supportCharset;
        }

        public boolean isSupportUnsigned() {
            return supportUnsigned;
        }

        public void setSupportUnsigned(boolean supportUnsigned) {
            this.supportUnsigned = supportUnsigned;
        }

        public boolean isSupportZeroFill() {
            return supportZeroFill;
        }

        public void setSupportZeroFill(boolean supportZeroFill) {
            this.supportZeroFill = supportZeroFill;
        }

        public boolean isSupportGeometry() {
            return supportGeometry;
        }

        public void setSupportGeometry(boolean supportGeometry) {
            this.supportGeometry = supportGeometry;
        }

        public boolean isSupportTimestamp() {
            return supportTimestamp;
        }

        public void setSupportTimestamp(boolean supportTimestamp) {
            this.supportTimestamp = supportTimestamp;
        }

        public boolean isSupportDefaultValue() {
            return supportDefaultValue;
        }

        public void setSupportDefaultValue(boolean supportDefaultValue) {
            this.supportDefaultValue = supportDefaultValue;
        }

        public boolean isSupportAutoIncrement() {
            return supportAutoIncrement;
        }

        public void setSupportAutoIncrement(boolean supportAutoIncrement) {
            this.supportAutoIncrement = supportAutoIncrement;
        }
    }

    public static boolean supportSize(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportSize;
            }
        }
        return false;
    }

    public static Integer suggestSize(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.suggestSize;
            }
        }
        return null;
    }

    public static boolean supportUnsigned(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportUnsigned;
            }
        }
        return false;
    }

    public static boolean supportJson(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportJson;
            }
        }
        return false;
    }

    public static boolean supportKeySize(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportKeySize;
            }
        }
        return false;
    }

    public static boolean supportString(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportString;
            }
        }
        return false;
    }

    public static boolean supportValue(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportValue;
            }
        }
        return false;
    }

    public static boolean supportZeroFill(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportZeroFill;
            }
        }
        return false;
    }

    public static boolean supportBit(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportBit;
            }
        }
        return false;
    }

    public static boolean supportBinary(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportBinary;
            }
        }
        return false;
    }

    public static boolean supportDigits(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportDigits;
            }
        }
        return false;
    }

    public static boolean supportDefaultValue(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportDefaultValue;
            }
        }
        return false;
    }

    public static boolean supportGeometry(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportGeometry;
            }
        }
        return false;
    }

    public static boolean supportEnum(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportEnum;
            }
        }
        return false;
    }

    public static boolean supportCharset(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportCharset;
            }
        }
        return false;
    }

    public static boolean supportTimestamp(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportTimestamp;
            }
        }
        return false;
    }

    public static boolean supportInteger(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportInteger;
            }
        }
        return false;
    }

    public static boolean supportAutoIncrement(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.supportAutoIncrement;
            }
        }
        return false;
    }

    public static Object exampleValue(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.exampleValue;
            }
        }
        return false;
    }

    public static Long minValue(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsIgnoreCase(value.name, type)) {
                return value.minValue;
            }
        }
        return null;
    }

    public static Long maxValue(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
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
