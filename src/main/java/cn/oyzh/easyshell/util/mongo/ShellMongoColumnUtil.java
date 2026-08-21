package cn.oyzh.easyshell.util.mongo;


import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBColumnField;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2024/1/29
 */
public class ShellMongoColumnUtil {

    private static final List<DBColumnField> COLUMN_FIELD = new ArrayList<>();

    static {

        DBColumnField objectFiled = new DBColumnField("OBJECT");
        objectFiled.supportJson = true;

        DBColumnField listField = new DBColumnField("LIST");
        listField.supportJsonArray = true;

        DBColumnField intField = new DBColumnField("INT");
        intField.supportInteger = true;

        DBColumnField bigintFiled = new DBColumnField("LONG");
        bigintFiled.supportBigInteger = true;

        DBColumnField stringField = new DBColumnField("STRING");
        stringField.supportString = true;

        DBColumnField booleanField = new DBColumnField("BOOLEAN");
        booleanField.supportBoolean = true;

        DBColumnField doubleField = new DBColumnField("DOUBLE");
        doubleField.supportDigits = true;

        DBColumnField binaryField = new DBColumnField("BINARY");
        binaryField.supportBinary = true;

        DBColumnField dateField = new DBColumnField("DATE");
        dateField.supportTimestamp = true;

        putFiled(objectFiled);
        putFiled(listField);
        putFiled(intField);
        putFiled(bigintFiled);
        putFiled(stringField);
        putFiled(booleanField);
        putFiled(doubleField);
        putFiled(binaryField);
        putFiled(dateField);
    }

    private static void putFiled(DBColumnField columnField) {
        COLUMN_FIELD.add(columnField);
    }

    public static List<String> fields() {
        return COLUMN_FIELD.parallelStream().map(DBColumnField::getName).collect(Collectors.toList());
    }

    public static boolean supportSize(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportSize;
            }
        }
        return false;
    }

    public static boolean supportJson(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportJson;
            }
        }
        return false;
    }

    public static boolean supportJsonArray(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportJsonArray;
            }
        }
        return false;
    }

    public static boolean supportString(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportString;
            }
        }
        return false;
    }

    public static boolean supportBinary(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportBinary;
            }
        }
        return false;
    }

    public static boolean supportBoolean(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportBoolean;
            }
        }
        return false;
    }

    public static boolean supportDigits(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportDigits;
            }
        }
        return false;
    }

    public static boolean supportTimestamp(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportTimestamp;
            }
        }
        return false;
    }

    public static boolean supportInteger(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportInteger;
            }
        }
        return false;
    }

    public static boolean supportBigInteger(String type) {
        for (DBColumnField value : COLUMN_FIELD) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportBigInteger;
            }
        }
        return false;
    }
}
