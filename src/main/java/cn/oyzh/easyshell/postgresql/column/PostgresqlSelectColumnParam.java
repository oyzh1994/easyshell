package cn.oyzh.easyshell.postgresql.column;

/**
 * PostgreSQL数据库查询字段参数
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlSelectColumnParam {

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 构造PostgreSQL数据库查询字段参数
     */
    public PostgresqlSelectColumnParam() {
    }

    /**
     * 构造PostgreSQL数据库查询字段参数
     *
     * @param schema    模式名称
     * @param tableName 表名称
     */
    public PostgresqlSelectColumnParam(String schema, String tableName) {
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
