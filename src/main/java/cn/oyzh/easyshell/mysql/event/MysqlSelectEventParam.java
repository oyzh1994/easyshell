package cn.oyzh.easyshell.mysql.event;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlSelectEventParam {

    private boolean full;

    private String dbName;

    private String eventName;

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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
