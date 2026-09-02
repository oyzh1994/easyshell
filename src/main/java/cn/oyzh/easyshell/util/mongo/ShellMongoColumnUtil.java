package cn.oyzh.easyshell.util.mongo;


import cn.oyzh.fx.db.DBColumnField;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;

/**
 * @author oyzh
 * @since 2024/1/29
 */
public class ShellMongoColumnUtil {

    /**
     * 初始化
     */
    public static void init() {
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
        DBColumnFieldManager.putFiled(DBDialect.MONGODB, columnField);
    }
}
