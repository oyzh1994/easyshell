package cn.oyzh.easyshell.h2.view;

/**
 * H2 修改视图参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2AlertViewParam {

    private String dbName = "main";

    private H2View view;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public H2AlertViewParam() {
    }

    public H2AlertViewParam(H2View view) {
        this.view = view;
    }

    public H2View getView() {
        return this.view;
    }

    public void setView(H2View view) {
        this.view = view;
    }
}
