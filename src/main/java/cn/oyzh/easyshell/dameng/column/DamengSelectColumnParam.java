package cn.oyzh.easyshell.dameng.column;

/**
 * 达梦数据库查询字段参数
 *
 * @author oyzh
 * @since 2024-09-14
 */
public class DamengSelectColumnParam {

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 构造达梦数据库查询字段参数
     */
    public DamengSelectColumnParam() {
    }

    /**
     * 构造达梦数据库查询字段参数
     *
     * @param schema    模式名称
     * @param tableName 表名称
     */
    public DamengSelectColumnParam(String schema, String tableName) {
        this.schema = schema;
        this.tableName = tableName;
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
     * 获取表名称
     *
     * @return 表名称
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 设置表名称
     *
     * @param tableName 表名称
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
