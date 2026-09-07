package cn.oyzh.easyshell.mysql.trigger;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlSelectTriggerParam {

    private boolean full;

    private String dbName;

    private String tableName;

    private String triggerName;

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }
}
