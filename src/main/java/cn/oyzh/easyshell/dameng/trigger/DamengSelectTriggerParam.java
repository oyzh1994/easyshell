package cn.oyzh.easyshell.dameng.trigger;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengSelectTriggerParam {

    private boolean full;

    private String schema;

    private String tableName;

    private String triggerName;

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
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
