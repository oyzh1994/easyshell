package cn.oyzh.easyshell.mysql.trigger;

/**
 * MySQL查询触发器参数
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlSelectTriggerParam {

    /**
     * 是否查询完整信息
     */
    private boolean full;

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 触发器名称
     */
    private String triggerName;

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
     * 获取表名称
     *
     * @return 表名称
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 设置表名称
     *
     * @param tableName 表名称
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 获取触发器名称
     *
     * @return 触发器名称
     */
    public String getTriggerName() {
        return triggerName;
    }

    /**
     * 设置触发器名称
     *
     * @param triggerName 触发器名称
     */
    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }
}
