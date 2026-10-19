package cn.oyzh.easyshell.mysql.generator.event;

import cn.oyzh.easyshell.mysql.event.MysqlEvent;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * MySQL修改事件SQL生成器
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlEventAlertSqlGenerator {

    /**
     * 生成修改事件SQL
     *
     * @param event 事件
     * @return SQL语句
     */
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
            sql += " \nCOMMENT " + DBUtil.wrapData(event.getComment(), DBDialect.MYSQL);
        }
        // 定义
        if (event.getDefinition() != null) {
            sql += " \nDO " + event.getDefinition();
        }
        return sql;
    }

    /**
     * 生成修改事件SQL
     *
     * @param event 事件
     * @return SQL语句
     */
    public static String generateSql(MysqlEvent event) {
        return new MysqlEventAlertSqlGenerator().generate(event);
    }
}
