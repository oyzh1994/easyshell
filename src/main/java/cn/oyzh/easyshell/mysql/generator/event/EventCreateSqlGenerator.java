package cn.oyzh.easyshell.mysql.generator.event;

import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;

/**
 * @author oyzh
 * @since 2024/09/09
 */
public abstract class EventCreateSqlGenerator {

    private DBDialect dialect;

    public EventCreateSqlGenerator(DBDialect dialect) {
        this.dialect = dialect;
    }

    public abstract String generate(MysqlEvent event);

    public static String generate(DBDialect dialect, MysqlEvent event) {
        return switch (dialect) {
            case MYSQL -> new MysqlEventCreateSqlGenerator().generate(event);
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
