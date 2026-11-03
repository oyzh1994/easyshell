package cn.oyzh.easyshell.sqlite.table;

/**
 * SQLite 查询表参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteSelectTableParam {

    private boolean full;

    private String dbName = "main";

    private String tableName;

    public SQLiteSelectTableParam() {
    }

    public SQLiteSelectTableParam(String tableName) {
        this.tableName = tableName;
    }

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
}
