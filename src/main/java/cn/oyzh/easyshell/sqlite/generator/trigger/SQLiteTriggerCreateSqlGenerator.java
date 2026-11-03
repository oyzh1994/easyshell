package cn.oyzh.easyshell.sqlite.generator.trigger;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteTrigger;
import cn.oyzh.fx.db.DBSqlGenerator;

import java.util.Locale;

/**
 * SQLite创建触发器SQL生成器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteTriggerCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段。
     *
     * @param trigger   触发器对象
     * @param tableName 表名
     */
    private void _generate(SQLiteTrigger trigger, String tableName) {
        String policy = StringUtil.blankToDefault(trigger.getPolicy(), "AFTER INSERT")
                .trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        String body = trigger.triggerBody();
        this.sqlBuilder.append("CREATE TRIGGER ")
                .append(SQLiteGeneratorUtil.quote(trigger.getName()))
                .append(' ').append(policy)
                .append(" ON ").append(SQLiteGeneratorUtil.quote(tableName))
                .append("\nBEGIN\n").append(body);
        if (!body.endsWith(";")) {
            this.sqlBuilder.append(';');
        }
        this.sqlBuilder.append("\nEND;");
    }

    /**
     * 生成单条SQL。
     *
     * @param trigger   触发器对象
     * @param tableName 表名
     * @return SQL语句
     */
    public String generateSingle(SQLiteTrigger trigger, String tableName) {
        this._generate(trigger, tableName);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL。
     *
     * @param trigger   触发器对象
     * @param tableName 表名
     * @return SQL语句
     */
    public static String generateSqlSingle(SQLiteTrigger trigger, String tableName) {
        return new SQLiteTriggerCreateSqlGenerator().generateSingle(trigger, tableName);
    }
}
