package cn.oyzh.easyshell.mysql.view;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlAlertViewParam {

    private String dbName;

    private MysqlView view;

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public MysqlView getView() {
        return view;
    }

    public void setView(MysqlView view) {
        this.view = view;
    }
}
