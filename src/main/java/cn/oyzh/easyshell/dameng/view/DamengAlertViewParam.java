package cn.oyzh.easyshell.dameng.view;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengAlertViewParam {

    private String schema;

    private DamengView view;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public DamengView getView() {
        return view;
    }

    public void setView(DamengView view) {
        this.view = view;
    }

    public String getViewName() {
        return this.view.getName();
    }

    public void setViewName(String viewName) {
        this.view.setName(viewName);
    }

}
