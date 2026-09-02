package cn.oyzh.easyshell.dameng.view;

/**
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengSelectViewParam {

    private boolean full;

    private String schema;

    private String viewName;

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}
