package cn.oyzh.easyshell.mysql.column;

/**
 * MySQL查询字段参数
 *
 * @author oyzh
 * @since 2024-09-14
 */
public class MysqlSelectColumnParam {

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 构造查询字段参数
     */
    public MysqlSelectColumnParam() {
    }

    /**
     * 构造查询字段参数
     *
     * @param dbName    库名称
     * @param tableName 表名称
     */
    public MysqlSelectColumnParam(String dbName, String tableName) {
        this.dbName = dbName;
        this.tableName = tableName;
    }

    /**
     * 构造查询字段参数
     *
     * @param dbName    库名称
     * @param schema    模式名称
     * @param tableName 表名称
     */
    public MysqlSelectColumnParam(String dbName, String schema, String tableName) {
        this.dbName = dbName;
        this.schema = schema;
        this.tableName = tableName;
    }


    /**
     * 获取库名称
     *
     * @return 库名称
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * 设置库名称
     *
     * @param dbName 库名称
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
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
