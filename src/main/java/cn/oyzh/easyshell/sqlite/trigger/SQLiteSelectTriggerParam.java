package cn.oyzh.easyshell.sqlite.trigger;

/**
 * SQLite 查询触发器参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteSelectTriggerParam {

    private boolean full;

    private String dbName = "main";

    private String tableName;

    private String triggerName;

    public boolean isFull() {
        return this.full;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTriggerName() {
        return this.triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }
}
