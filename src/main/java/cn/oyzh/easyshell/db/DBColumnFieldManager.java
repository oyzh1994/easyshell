package cn.oyzh.easyshell.db;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.DBDialect;
import cn.oyzh.easyshell.util.mongo.ShellMongoColumnUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlColumnUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author oyzh
 * @since 2026-08-26
 */
public class DBColumnFieldManager {

    private static final Map<DBDialect, List<DBColumnField>> COLUMN_FIELD = new HashMap<>();

    public static void putFiled(DBDialect dialect, DBColumnField columnField) {
        List<DBColumnField> list = COLUMN_FIELD.get(dialect);
        if (list == null) {
            list = new ArrayList<>();
            list.add(columnField);
            COLUMN_FIELD.put(dialect, list);
        } else {
            list.add(columnField);
        }
    }

    public static List<DBColumnField> fields(DBDialect dialect) {
        if (!COLUMN_FIELD.containsKey(dialect)) {
            if (dialect == DBDialect.MYSQL) {
                ShellMysqlColumnUtil.init();
            } else if (dialect == DBDialect.MONGODB) {
                ShellMongoColumnUtil.init();
            }
        }
        List<DBColumnField> list = COLUMN_FIELD.get(dialect);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public static List<String> fieldNames(DBDialect dialect) {
        return fields(dialect).parallelStream().map(DBColumnField::getName).collect(Collectors.toList());
    }

    public static boolean supportSize(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportSize;
            }
        }
        return false;
    }

    public static Integer suggestSize(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.suggestSize;
            }
        }
        return null;
    }

    public static boolean supportUnsigned(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportUnsigned;
            }
        }
        return false;
    }

    public static boolean supportJson(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportJson;
            }
        }
        return false;
    }

    public static boolean supportKeySize(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportKeySize;
            }
        }
        return false;
    }

    public static boolean supportString(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportString;
            }
        }
        return false;
    }

    public static boolean supportText(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportText;
            }
        }
        return false;
    }

    public static boolean supportValue(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportValue;
            }
        }
        return false;
    }

    public static boolean supportZeroFill(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportZeroFill;
            }
        }
        return false;
    }

    public static boolean supportBit(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportBit;
            }
        }
        return false;
    }

    public static boolean supportBinary(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportBinary;
            }
        }
        return false;
    }

    public static boolean supportDigits(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportDigits;
            }
        }
        return false;
    }

    public static boolean supportDefaultValue(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportDefaultValue;
            }
        }
        return false;
    }

    public static boolean supportGeometry(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportGeometry;
            }
        }
        return false;
    }

    public static boolean supportEnum(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportEnum;
            }
        }
        return false;
    }

    public static boolean supportCharset(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportCharset;
            }
        }
        return false;
    }

    public static boolean supportTimestamp(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportTimestamp;
            }
        }
        return false;
    }

    public static boolean supportInteger(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportInteger;
            }
        }
        return false;
    }

    public static boolean supportAutoIncrement(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportAutoIncrement;
            }
        }
        return false;
    }

    public static Object exampleValue(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.exampleValue;
            }
        }
        return false;
    }

    public static Long minValue(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.minValue;
            }
        }
        return null;
    }

    public static Long maxValue(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.maxValue;
            }
        }
        return null;
    }

    public static boolean supportJsonArray(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportJsonArray;
            }
        }
        return false;
    }

    public static boolean supportBigInteger(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportBigInteger;
            }
        }
        return false;
    }

    public static boolean supportBoolean(DBDialect dialect, String type) {
        for (DBColumnField value : fields(dialect)) {
            if (StringUtil.equalsAnyIgnoreCase(type, value.name, value.alias)) {
                return value.supportBoolean;
            }
        }
        return false;
    }
}
