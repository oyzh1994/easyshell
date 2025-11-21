package cn.oyzh.easyshell.mysql.view;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlSelectViewParam {

    private boolean full;

    private String dbName;

    private String viewName;

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

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}
