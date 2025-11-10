package cn.oyzh.easyshell.db.event;

import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.mysql.generator.event.MysqlEventAlertSqlGenerator;

/**
 * @author oyzh
 * @since 2024/09/09
 */
public abstract class DBEventAlertSqlGenerator {

    private DBDialect dialect;

    public DBEventAlertSqlGenerator(DBDialect dialect) {
        this.dialect = dialect;
    }

    public abstract String generate(MysqlEvent event);

    public static String generate(DBDialect dialect, MysqlEvent event) {
        return switch (dialect) {
            case MYSQL -> new MysqlEventAlertSqlGenerator().generate(event);
            default -> null;
        };
    }

    public DBDialect getDialect() {
        return dialect;
    }

    public void setDialect(DBDialect dialect) {
        this.dialect = dialect;
    }
}
