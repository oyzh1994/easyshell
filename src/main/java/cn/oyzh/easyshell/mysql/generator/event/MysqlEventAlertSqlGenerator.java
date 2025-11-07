package cn.oyzh.easyshell.mysql.generator.event;

import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.easyshell.util.mysql.DBUtil;

/**
 * @author oyzh
 * @since 2024-09-10
 */
public class MysqlEventAlertSqlGenerator extends EventAlertSqlGenerator {

    public MysqlEventAlertSqlGenerator() {
        super(DBDialect.MYSQL);
    }

    protected MysqlEventAlertSqlGenerator(DBDialect dialect) {
        super(dialect);
    }

    @Override
    public String generate(MysqlEvent event) {
        // 起始
        String sql = "ALTER ";
        // 定义者
        if (event.getDefiner() != null) {
            sql += " DEFINER = " + event.getDefiner();
        }
        // 名称
        sql += " EVENT " + DBUtil.wrap(event.getDbName(), event.getName(), DBDialect.MYSQL);
        // 执行时间
        sql += "\nON SCHEDULE ";
        if (event.isOnTimeType()) {
            sql += "AT " + event.executeAt();
            if (event.getIntervalValue() != null) {
                sql += " + INTERVAL '" + event.getIntervalValue() + "' " + event.getIntervalField();
            }
        } else {
            sql += "\nEVERY '" + event.getIntervalValue() + "' " + event.getIntervalField();
            if (event.getStarts() != null) {
                sql += " STARTS " + event.starts();
                if (event.getStartIntervalValue() != null) {
                    sql += " + INTERVAL '" + event.getStartIntervalValue() + "' " + event.getStartIntervalField();
                }
            }
            if (event.getEnds() != null) {
                sql += " ENDS " + event.ends();
                if (event.getEndIntervalValue() != null) {
                    sql += " + INTERVAL '" + event.getEndIntervalValue() + "' " + event.getEndIntervalField();
                }
            }
        }
        // 完成时
        if (event.getOnCompletion() != null) {
            sql += " \nON COMPLETION " + event.getOnCompletion();
        }
        // 状态
        if (event.getStatus() != null) {
            sql += " \n" + event.getStatus();
        }
        // 注释
        if (event.getComment() != null) {
            sql += " \nCOMMENT " + DBUtil.wrapData(event.getComment());
        }
        // 定义
        if (event.getDefinition() != null) {
            sql += " \nDO " + event.getDefinition();
        }
        return sql;
    }
}
