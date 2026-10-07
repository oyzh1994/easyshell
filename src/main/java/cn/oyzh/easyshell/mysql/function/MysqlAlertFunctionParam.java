package cn.oyzh.easyshell.mysql.function;

/**
 * MySQL修改函数参数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlAlertFunctionParam {

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 函数
     */
    private MysqlFunction function;

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
     * 获取函数
     *
     * @return 函数
     */
    public MysqlFunction getFunction() {
        return function;
    }

    /**
     * 设置函数
     *
     * @param function 函数
     */
    public void setFunction(MysqlFunction function) {
        this.function = function;
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String getFunctionName() {
        return this.function.getName();
    }

    /**
     * 设置函数名称
     *
     * @param functionName 函数名称
     */
    public void setFunctionName(String functionName) {
        this.function.setName(functionName);
    }
}
