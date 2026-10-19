package cn.oyzh.easyshell.mariadb.event;

/**
 * MariaDB查询事件参数
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbSelectEventParam {

    /**
     * 是否查询完整信息
     */
    private boolean full;

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 事件名称
     */
    private String eventName;

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
     * 获取事件名称
     *
     * @return 事件名称
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * 设置事件名称
     *
     * @param eventName 事件名称
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
