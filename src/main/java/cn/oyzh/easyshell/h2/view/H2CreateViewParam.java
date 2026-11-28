package cn.oyzh.easyshell.h2.view;

/**
 * H2 创建视图参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2CreateViewParam {

    private String dbName = "main";

    private H2View view;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public H2CreateViewParam() {
    }

    public H2CreateViewParam(H2View view) {
        this.view = view;
    }

    public H2View getView() {
        return this.view;
    }

    public void setView(H2View view) {
        this.view = view;
    }

    public String getViewName() {
        return this.view.getName();
    }

    public void setViewName(String viewName) {
        this.view.setName(viewName);
    }
}
