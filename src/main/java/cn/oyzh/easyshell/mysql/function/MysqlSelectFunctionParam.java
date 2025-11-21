package cn.oyzh.easyshell.mysql.function;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlSelectFunctionParam {

    private boolean full;

    private String dbName;

    private String functionName;

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

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}
