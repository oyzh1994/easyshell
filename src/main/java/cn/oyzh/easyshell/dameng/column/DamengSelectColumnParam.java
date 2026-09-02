package cn.oyzh.easyshell.dameng.column;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengSelectColumnParam {

    private String schema;

    private String tableName;

    public DamengSelectColumnParam() {
    }

    public DamengSelectColumnParam(String schema, String tableName) {
        this.schema = schema;
        this.tableName = tableName;
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
