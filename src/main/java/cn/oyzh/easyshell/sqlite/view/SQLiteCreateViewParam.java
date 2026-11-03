package cn.oyzh.easyshell.sqlite.view;

/**
 * SQLite 创建视图参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteCreateViewParam {

    private String dbName = "main";

    private SQLiteView view;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public SQLiteCreateViewParam() {
    }

    public SQLiteCreateViewParam(SQLiteView view) {
        this.view = view;
    }

    public SQLiteView getView() {
        return this.view;
    }

    public void setView(SQLiteView view) {
        this.view = view;
    }

    public String getViewName() {
        return this.view.getName();
    }

    public void setViewName(String viewName) {
        this.view.setName(viewName);
    }
}
