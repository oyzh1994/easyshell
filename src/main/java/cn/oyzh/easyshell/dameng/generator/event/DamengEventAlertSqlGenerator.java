package cn.oyzh.easyshell.dameng.generator.event;//package cn.oyzh.easydameng.generator.event;
//
//import cn.oyzh.fx.db.DBDialect;
//import cn.oyzh.easyshell.dameng.event.DamengEvent;
//import cn.oyzh.easyshell.util.dameng.DBUtil;
//
///**
// * @author oyzh
// * @since 2024-09-10
// */
//public class DamengEventAlertSqlGenerator extends EventAlertSqlGenerator {
//
//    @Override
//    public String generate(DamengEvent event) {
//        // 起始
//        String sql = "ALTER ";
//        // 定义者
//        if (event.getDefiner() != null) {
//            sql += " DEFINER = " + event.getDefiner();
//        }
//        // 名称
//        sql += " EVENT " + DBUtil.wrap(event.getDbName(), event.getName(), DBDialect.DAMENG);
//        // 执行时间
//        sql += "\nON SCHEDULE ";
//        if (event.isOnTimeType()) {
//            sql += "AT " + event.executeAt();
//            if (event.getIntervalValue() != null) {
//                sql += " + INTERVAL '" + event.getIntervalValue() + "' " + event.getIntervalField();
//            }
//        } else {
//            sql += "\nEVERY '" + event.getIntervalValue() + "' " + event.getIntervalField();
//            if (event.getStarts() != null) {
//                sql += " STARTS " + event.starts();
//                if (event.getStartIntervalValue() != null) {
//                    sql += " + INTERVAL '" + event.getStartIntervalValue() + "' " + event.getStartIntervalField();
//                }
//            }
//            if (event.getEnds() != null) {
//                sql += " ENDS " + event.ends();
//                if (event.getEndIntervalValue() != null) {
//                    sql += " + INTERVAL '" + event.getEndIntervalValue() + "' " + event.getEndIntervalField();
//                }
//            }
//        }
//        // 完成时
//        if (event.getOnCompletion() != null) {
//            sql += " \nON COMPLETION " + event.getOnCompletion();
//        }
//        // 状态
//        if (event.getStatus() != null) {
//            sql += " \n" + event.getStatus();
//        }
//        // 注释
//        if (event.getComment() != null) {
//            sql += " \nCOMMENT " + DBUtil.wrapData(event.getComment());
//        }
//        // 定义
//        if (event.getDefinition() != null) {
//            sql += " \nDO " + event.getDefinition();
//        }
//        return sql;
//    }
//}
