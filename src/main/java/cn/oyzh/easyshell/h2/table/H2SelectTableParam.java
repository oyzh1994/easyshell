package cn.oyzh.easyshell.h2.table;

/**
 * H2 查询表参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2SelectTableParam {

    private boolean full;

    private String dbName = "main";

    private String tableName;

    public H2SelectTableParam() {
    }

    public H2SelectTableParam(String tableName) {
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
