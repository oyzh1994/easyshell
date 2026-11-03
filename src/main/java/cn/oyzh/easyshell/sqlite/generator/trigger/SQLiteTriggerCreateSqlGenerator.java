package cn.oyzh.easyshell.sqlite.generator.trigger;

import cn.oyzh.easyshell.sqlite.trigger.SQLiteTrigger;
import cn.oyzh.fx.db.DBSqlGenerator;

/**
 * SQLite 创建触发器 SQL 生成器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteTriggerCreateSqlGenerator extends DBSqlGenerator {

    public String generateSingle(SQLiteTrigger trigger) {
        this.sqlBuilder.append(trigger.normalizedDefinition(null, trigger.getTableName()));
        return this.buildSqlSingle();
    }

    public static String generateSqlSingle(SQLiteTrigger trigger) {
        return new SQLiteTriggerCreateSqlGenerator().generateSingle(trigger);
    }
}
