package cn.oyzh.easyshell.sqlite.view;

/**
 * SQLite 修改视图参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteAlertViewParam {

    private String dbName = "main";

    private SQLiteView view;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public SQLiteAlertViewParam() {
    }

    public SQLiteAlertViewParam(SQLiteView view) {
        this.view = view;
    }

    public SQLiteView getView() {
        return this.view;
    }

    public void setView(SQLiteView view) {
        this.view = view;
    }
}
