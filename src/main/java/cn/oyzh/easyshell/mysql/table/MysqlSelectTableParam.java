package cn.oyzh.easyshell.mysql.table;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlSelectTableParam {

    private boolean full;

    private String dbName;

    private String tableName;

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
}
