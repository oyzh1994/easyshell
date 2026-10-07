package cn.oyzh.easyshell.mysql.function;

/**
 * MySQL查询函数参数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlSelectFunctionParam {

    /**
     * 是否查询完整信息
     */
    private boolean full;

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 函数名称
     */
    private String functionName;

    /**
     * 是否查询完整信息
     *
     * @return 结果
     */
    public boolean isFull() {
        return full;
    }

    /**
     * 设置是否查询完整信息
     *
     * @param full 是否查询完整信息
     */
    public void setFull(boolean full) {
        this.full = full;
    }

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
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * 设置函数名称
     *
     * @param functionName 函数名称
     */
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}
