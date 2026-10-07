package cn.oyzh.easyshell.dameng.view;

/**
 * 达梦查询视图参数
 *
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengSelectViewParam {

    /**
     * 是否查询完整信息
     */
    private boolean full;

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 视图名称
     */
    private String viewName;

    /**
     * 是否查询完整信息
     *
     * @return 结果
     */
    public boolean isFull() {
        return full;
    }

    /**
     * 设置是否查询完整信息
     *
     * @param full 是否查询完整信息
     */
    public void setFull(boolean full) {
        this.full = full;
    }

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
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * 设置视图名称
     *
     * @param viewName 视图名称
     */
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}
