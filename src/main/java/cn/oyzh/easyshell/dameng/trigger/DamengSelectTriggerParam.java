package cn.oyzh.easyshell.dameng.trigger;

/**
 * 达梦查询触发器参数
 *
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengSelectTriggerParam {

    /**
     * 是否查询完整信息
     */
    private boolean full;

    /**
     * 模式名称
     */
    private String schema;

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
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 设置模式名称
     *
     * @param schema 模式名称
     */
    public void setSchema(String schema) {
        this.schema = schema;
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
