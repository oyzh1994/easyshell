//package cn.oyzh.easyshell.sqlite.generator.trigger;
//
//import cn.oyzh.easyshell.sqlite.trigger.SQLiteTrigger;
//import cn.oyzh.fx.db.DBSqlGenerator;
//
///**
// * SQLite创建触发器SQL生成器
// *
// * @author oyzh
// * @since 2026-11-02
// */
//public class SQLiteTriggerCreateSqlGenerator extends DBSqlGenerator {
//
//    /**
//     * 生成SQL片段
//     *
//     * @param trigger 触发器对象
//     */
//    private void _generate(SQLiteTrigger trigger) {
//        this.sqlBuilder.append(trigger.normalizedDefinition(null, trigger.getTableName()));
//    }
//
//    /**
//     * 生成单条SQL
//     *
//     * @param trigger 触发器对象
//     * @return SQL语句
//     */
//    public String generateSingle(SQLiteTrigger trigger) {
//        this._generate(trigger);
//        return this.buildSqlSingle();
//    }
//
//    /**
//     * 生成单条SQL
//     *
//     * @param trigger 触发器对象
//     * @return SQL语句
//     */
//    public static String generateSqlSingle(SQLiteTrigger trigger) {
//        return new SQLiteTriggerCreateSqlGenerator().generateSingle(trigger);
//    }
//}
