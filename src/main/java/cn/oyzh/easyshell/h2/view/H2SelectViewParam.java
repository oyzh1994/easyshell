package cn.oyzh.easyshell.h2.view;

/**
 * H2 查询视图参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2SelectViewParam {

    private boolean full;

    private String dbName = "main";

    private String viewName;

    public H2SelectViewParam() {
    }

    public H2SelectViewParam(String viewName) {
        this.viewName = viewName;
    }

    public boolean isFull() {
        return this.full;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public String getViewName() {
        return this.viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}
