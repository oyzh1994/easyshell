package cn.oyzh.easyshell.mysql.procedure;

/**
 * MySQL创建存储过程参数
 *
 * @author oyzh
 * @since 2026-08-28
 */
public class MysqlCreateProcedureParam {

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 存储过程
     */
    private MysqlProcedure procedure;

    /**
     * 获取库名称
     *
     * @return 库名称
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * 设置库名称
     *
     * @param dbName 库名称
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * 获取存储过程
     *
     * @return 存储过程
     */
    public MysqlProcedure getProcedure() {
        return procedure;
    }

    /**
     * 设置存储过程
     *
     * @param procedure 存储过程
     */
    public void setProcedure(MysqlProcedure procedure) {
        this.procedure = procedure;
    }

    /**
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String getProcedureName() {
        return this.procedure.getName();
    }

    /**
     * 设置存储过程名称
     *
     * @param functionName 存储过程名称
     */
    public void setProcedureName(String functionName) {
        this.procedure.setName(functionName);
    }
}
