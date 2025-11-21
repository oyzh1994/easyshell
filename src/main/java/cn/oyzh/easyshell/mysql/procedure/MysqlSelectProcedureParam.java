package cn.oyzh.easyshell.mysql.procedure;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlSelectProcedureParam {

    private boolean full;

    private String dbName;

    private String procedureName;

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

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }
}
