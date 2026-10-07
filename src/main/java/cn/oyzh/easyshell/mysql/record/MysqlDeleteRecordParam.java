package cn.oyzh.easyshell.mysql.record;


import cn.oyzh.fx.db.DBRecordData;

/**
 * MySQL删除记录参数
 *
 * @author oyzh
 * @since 2024-09-13
 */
public class MysqlDeleteRecordParam {

    /**
     * 库名称
     */
    private String dbName;

    // private String schema;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 记录数据
     */
    private DBRecordData record;

    /**
     * 主键
     */
    private MysqlRecordPrimaryKey primaryKey;

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

    // public String getSchema() {
    //     return schema;
    // }
    //
    // public void setSchema(String schema) {
    //     this.schema = schema;
    // }

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

    /**
     * 获取记录数据
     *
     * @return 记录数据
     */
    public DBRecordData getRecord() {
        return record;
    }

    /**
     * 设置记录数据
     *
     * @param record 记录数据
     */
    public void setRecord(DBRecordData record) {
        this.record = record;
    }

    /**
     * 获取主键
     *
     * @return 主键
     */
    public MysqlRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * 设置主键
     *
     * @param primaryKey 主键
     */
    public void setPrimaryKey(MysqlRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
