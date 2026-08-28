package cn.oyzh.easyshell.mysql.procedure;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlAlertProcedureParam {

    private String dbName;

    private MysqlProcedure procedure;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public MysqlProcedure getProcedure() {
        return procedure;
    }

    public void setProcedure(MysqlProcedure procedure) {
        this.procedure = procedure;
    }

    public String getProcedureName() {
        return this.procedure.getName();
    }

    public void setProcedureName(String functionName) {
        this.procedure.setName(functionName);
    }
}
