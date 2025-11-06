package cn.oyzh.easyshell.mysql.column;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlSelectColumnParam {

    private String dbName;

    private String schema;

    private String tableName;

    public MysqlSelectColumnParam() {
    }

    public MysqlSelectColumnParam(String dbName, String tableName) {
        this.dbName = dbName;
        this.tableName = tableName;
    }

    public MysqlSelectColumnParam(String dbName, String schema, String tableName) {
        this.dbName = dbName;
        this.schema = schema;
        this.tableName = tableName;
    }


    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
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
