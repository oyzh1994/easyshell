package cn.oyzh.easyshell.mysql.procedure;

/**
 * MySQL查询存储过程参数
 *
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlSelectProcedureParam {

    /**
     * 是否查询完整信息
     */
    private boolean full;

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 存储过程名称
     */
    private String procedureName;

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
     * 获取存储过程名称
     *
     * @return 存储过程名称
     */
    public String getProcedureName() {
        return procedureName;
    }

    /**
     * 设置存储过程名称
     *
     * @param procedureName 存储过程名称
     */
    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }
}
