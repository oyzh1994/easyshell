package cn.oyzh.easyshell.mysql.function;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlAlertFunctionParam {

    private String dbName;

    private MysqlFunction function;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public MysqlFunction getFunction() {
        return function;
    }

    public void setFunction(MysqlFunction function) {
        this.function = function;
    }

    public String getFunctionName() {
        return this.function.getName();
    }

    public void setFunctionName(String functionName) {
        this.function.setName(functionName);
    }
}
