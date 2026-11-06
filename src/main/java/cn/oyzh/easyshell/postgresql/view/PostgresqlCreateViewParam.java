package cn.oyzh.easyshell.postgresql.view;

import cn.oyzh.easyshell.postgresql.view.PostgresqlView;

/**
 * PostgreSQL创建视图参数
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlCreateViewParam {

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 视图
     */
    private PostgresqlView view;

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 设置模式名称
     *
     * @param schema 模式名称
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 获取视图
     *
     * @return 视图
     */
    public PostgresqlView getView() {
        return view;
    }

    /**
     * 设置视图
     *
     * @param view 视图
     */
    public void setView(PostgresqlView view) {
        this.view = view;
    }

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String getViewName() {
        return this.view.getName();
    }

    /**
     * 设置视图名称
     *
     * @param viewName 视图名称
     */
    public void setViewName(String viewName) {
        this.view.setName(viewName);
    }
}
