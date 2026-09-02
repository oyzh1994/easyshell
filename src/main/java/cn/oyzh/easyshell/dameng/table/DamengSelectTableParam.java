package cn.oyzh.easyshell.dameng.table;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengSelectTableParam {

    private boolean full;

    private String schema;

    private String tableName;

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
}
