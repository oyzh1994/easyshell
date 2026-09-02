package cn.oyzh.easyshell.util.dameng;


import cn.oyzh.fx.db.DBColumnField;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2024/1/29
 */
public class DamengColumnUtil {

    /**
     * 初始化
     */
    public static void init() {
        // ===== 字符类型 =====
        DBColumnField charFiled = new DBColumnField("CHAR");
        charFiled.suggestSize = 255;
        charFiled.supportSize = true;
        charFiled.supportString = true;
        charFiled.supportDefaultValue = true;

        DBColumnField varcharFiled = new DBColumnField("VARCHAR2");
        varcharFiled.suggestSize = 255;
        varcharFiled.supportSize = true;
        varcharFiled.supportString = true;
        varcharFiled.supportDefaultValue = true;

        DBColumnField varchar2Field = new DBColumnField("VARCHAR");
        varchar2Field.suggestSize = 255;
        varchar2Field.supportSize = true;
        varchar2Field.supportString = true;
        varchar2Field.supportDefaultValue = true;

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
        // varbinaryField.supportSize = true;
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

        //        DBColumnField booleanFiled = new DBColumnField("BOOLEAN");
        ////        booleanFiled.supportBit = true;
        //        booleanFiled.supportDefaultValue = true;

//        // ===== 大文本类型（JSON数据用CLOB模拟） =====
//        DBColumnField jsonField = new DBColumnField("CLOB");
//        jsonField.supportJson = true;
//        jsonField.supportString = true;

        // ===== 注册到列表 =====
        putFiled(charFiled);
        putFiled(varcharFiled);
        putFiled(varchar2Field);
        putFiled(clobField);
        putFiled(ncharField);
        putFiled(nvarchar2Filed);

        putFiled(numberField);
        putFiled(intField);
        putFiled(integerField);
        putFiled(bigintFiled);
        putFiled(smallintFiled);
        putFiled(tinyintField);
        putFiled(floatField);
        putFiled(doubleField);
        putFiled(doublePrecisionField);
        putFiled(decimalField);
        putFiled(numericField);

        putFiled(dateField);
        putFiled(timestampField);
        putFiled(datetimeField);
        putFiled(timeField);
        putFiled(timeWithTzField);
        putFiled(timestampWithTzField);
        putFiled(timestampWithLocalTzField);
        putFiled(intervalYmField);
        putFiled(intervalDsField);

        putFiled(blobField);
        putFiled(imageField);
        putFiled(rawField);
        putFiled(varbinaryField);
        putFiled(binaryField);

        putFiled(bitFiled);
        //putFiled(booleanFiled);
//        putFiled(jsonField);
    }

    private static void putFiled(DBColumnField columnField) {
        DBColumnFieldManager.putFiled(DBDialect.DAMENG, columnField);
    }

    public static List<String> fields() {
        return DBColumnFieldManager.fields(DBDialect.DAMENG).parallelStream().map(DBColumnField::getName).collect(Collectors.toList());
    }
    //
    //    private static class DBColumnField {
    //
    //        private String name;
    //
    //        private Long maxValue;
    //
    //        private Long minValue;
    //
    //        /**
    //         * 推荐字段长
    //         */
    //        private Integer suggestSize;
    //
    //        private boolean supportBit;
    //
    //        private String exampleValue;
    //
    //        private boolean supportSize;
    //
    //        private boolean supportJson;
    //
    //        private boolean supportText;
    //
    //        private boolean supportValue;
    //
    //        private boolean supportBinary;
    //
    //        private boolean supportDigits;
    //
    //        private boolean supportString;
    //
    //        private boolean supportKeySize;
    //
    //        private boolean supportInteger;
    //
    //        private boolean supportUnsigned;
    //
    //        private boolean supportTimestamp;
    //
    //        private boolean supportDefaultValue;
    //
    //        private boolean supportAutoIncrement;
    //
    //        public DBColumnField(String name) {
    //            this.name = name;
    //        }
    //
    //        public String getName() {
    //            return name;
    //        }
    //
    //        public void setName(String name) {
    //            this.name = name;
    //        }
    //
    //        public Long getMaxValue() {
    //            return maxValue;
    //        }
    //
    //        public void setMaxValue(Long maxValue) {
    //            this.maxValue = maxValue;
    //        }
    //
    //        public Long getMinValue() {
    //            return minValue;
    //        }
    //
    //        public void setMinValue(Long minValue) {
    //            this.minValue = minValue;
    //        }
    //
    //        public Integer getSuggestSize() {
    //            return suggestSize;
    //        }
    //
    //        public void setSuggestSize(Integer suggestSize) {
    //            this.suggestSize = suggestSize;
    //        }
    //
    //        public boolean isSupportBit() {
    //            return supportBit;
    //        }
    //
    //        public void setSupportBit(boolean supportBit) {
    //            this.supportBit = supportBit;
    //        }
    //
    //        public String getExampleValue() {
    //            return exampleValue;
    //        }
    //
    //        public void setExampleValue(String exampleValue) {
    //            this.exampleValue = exampleValue;
    //        }
    //
    //        public boolean isSupportSize() {
    //            return supportSize;
    //        }
    //
    //        public void setSupportSize(boolean supportSize) {
    //            this.supportSize = supportSize;
    //        }
    //
    //        public boolean isSupportJson() {
    //            return supportJson;
    //        }
    //
    //        public void setSupportJson(boolean supportJson) {
    //            this.supportJson = supportJson;
    //        }
    //
    //        public boolean isSupportValue() {
    //            return supportValue;
    //        }
    //
    //        public void setSupportValue(boolean supportValue) {
    //            this.supportValue = supportValue;
    //        }
    //
    //        public boolean isSupportBinary() {
    //            return supportBinary;
    //        }
    //
    //        public void setSupportBinary(boolean supportBinary) {
    //            this.supportBinary = supportBinary;
    //        }
    //
    //        public boolean isSupportDigits() {
    //            return supportDigits;
    //        }
    //
    //        public void setSupportDigits(boolean supportDigits) {
    //            this.supportDigits = supportDigits;
    //        }
    //
    //        public boolean isSupportString() {
    //            return supportString;
    //        }
    //
    //        public void setSupportString(boolean supportString) {
    //            this.supportString = supportString;
    //        }
    //
    //        public boolean isSupportKeySize() {
    //            return supportKeySize;
    //        }
    //
    //        public void setSupportKeySize(boolean supportKeySize) {
    //            this.supportKeySize = supportKeySize;
    //        }
    //
    //        public boolean isSupportInteger() {
    //            return supportInteger;
    //        }
    //
    //        public void setSupportInteger(boolean supportInteger) {
    //            this.supportInteger = supportInteger;
    //        }
    //
    //        public boolean isSupportUnsigned() {
    //            return supportUnsigned;
    //        }
    //
    //        public void setSupportUnsigned(boolean supportUnsigned) {
    //            this.supportUnsigned = supportUnsigned;
    //        }
    //
    ////        public boolean isSupportGeometry() {
    ////            return supportGeometry;
    ////        }
    ////
    ////        public void setSupportGeometry(boolean supportGeometry) {
    ////            this.supportGeometry = supportGeometry;
    ////        }
    //
    //        public boolean isSupportTimestamp() {
    //            return supportTimestamp;
    //        }
    //
    //        public void setSupportTimestamp(boolean supportTimestamp) {
    //            this.supportTimestamp = supportTimestamp;
    //        }
    //
    //        public boolean isSupportDefaultValue() {
    //            return supportDefaultValue;
    //        }
    //
    //        public void setSupportDefaultValue(boolean supportDefaultValue) {
    //            this.supportDefaultValue = supportDefaultValue;
    //        }
    //
    //        public boolean isSupportAutoIncrement() {
    //            return supportAutoIncrement;
    //        }
    //
    //        public void setSupportAutoIncrement(boolean supportAutoIncrement) {
    //            this.supportAutoIncrement = supportAutoIncrement;
    //        }
    //    }
    //
    //    public static boolean supportSize(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportSize;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static Integer suggestSize(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.suggestSize;
    //            }
    //        }
    //        return null;
    //    }
    //
    //    public static boolean supportUnsigned(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportUnsigned;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static boolean supportJson(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportJson;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static boolean supportText(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportText;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static boolean supportKeySize(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportKeySize;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static boolean supportString(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportString;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static boolean supportValue(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportValue;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static boolean supportBit(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportBit;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static boolean supportBinary(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportBinary;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static boolean supportDigits(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportDigits;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static boolean supportDefaultValue(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportDefaultValue;
    //            }
    //        }
    //        return false;
    //    }
    //

    /// /    public static boolean supportGeometry(String type) {
    /// /        for (DBColumnField value : COLUMN_FIELD) {
    /// /            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    /// /                return value.supportGeometry;
    /// /            }
    /// /        }
    /// /        return false;
    /// /    }
    /// /
    /// /    public static boolean supportEnum(String type) {
    /// /        for (DBColumnField value : COLUMN_FIELD) {
    /// /            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    /// /                return value.supportEnum;
    /// /            }
    /// /        }
    /// /        return false;
    /// /    }
    //
    //    public static boolean supportTimestamp(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportTimestamp;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static boolean supportInteger(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportInteger;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static boolean supportAutoIncrement(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.supportAutoIncrement;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static Object exampleValue(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.exampleValue;
    //            }
    //        }
    //        return false;
    //    }
    //
    //    public static Long minValue(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.minValue;
    //            }
    //        }
    //        return null;
    //    }
    //
    //    public static Long maxValue(String type) {
    //        for (DBColumnField value : COLUMN_FIELD) {
    //            if (StringUtil.equalsIgnoreCase(value.name, type)) {
    //                return value.maxValue;
    //            }
    //        }
    //        return null;
    //    }
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
}
