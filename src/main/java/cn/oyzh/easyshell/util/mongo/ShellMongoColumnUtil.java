package cn.oyzh.easyshell.util.mongo;


import cn.oyzh.fx.db.DBColumnField;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;

/**
 * @author oyzh
 * @since 2026-08-21
 */
public class ShellMongoColumnUtil {

    private static boolean initialized;

    /**
     * 初始化
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

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

        DBColumnField decimalField = new DBColumnField("DECIMAL");
        decimalField.supportDigits = true;

        DBColumnField binaryField = new DBColumnField("BINARY");
        binaryField.supportBinary = true;

        DBColumnField dateField = new DBColumnField("DATE");
        dateField.supportTimestamp = true;

        DBColumnField regexField = new DBColumnField("REGEX");
        regexField.supportString = true;

        // 常用类型优先
        putFiled(stringField);
        putFiled(intField);
        putFiled(bigintFiled);
        putFiled(doubleField);
        putFiled(decimalField);
        putFiled(booleanField);
        putFiled(dateField);
        putFiled(regexField);

        // 低频和复合类型
        putFiled(objectFiled);
        putFiled(listField);
        putFiled(binaryField);
    }

    private static void putFiled(DBColumnField columnField) {
        DBColumnFieldManager.putFiled(DBDialect.MONGODB, columnField);
    }
}
