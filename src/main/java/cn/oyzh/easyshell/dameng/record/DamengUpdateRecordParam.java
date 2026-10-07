package cn.oyzh.easyshell.dameng.record;


import cn.oyzh.fx.db.DBRecordData;

/**
 * 达梦更新记录参数
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengUpdateRecordParam {

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 原始记录数据
     */
    private DBRecordData record;

    /**
     * 待更新记录数据
     */
    private DBRecordData updateRecord;

    /**
     * 主键
     */
    private DamengRecordPrimaryKey primaryKey;

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

    /**
     * 获取原始记录数据
     *
     * @return 原始记录数据
     */
    public DBRecordData getRecord() {
        return record;
    }

    /**
     * 设置原始记录数据
     *
     * @param record 原始记录数据
     */
    public void setRecord(DBRecordData record) {
        this.record = record;
    }

    /**
     * 获取待更新记录数据
     *
     * @return 待更新记录数据
     */
    public DBRecordData getUpdateRecord() {
        return updateRecord;
    }

    /**
     * 设置待更新记录数据
     *
     * @param updateRecord 待更新记录数据
     */
    public void setUpdateRecord(DBRecordData updateRecord) {
        this.updateRecord = updateRecord;
    }

    /**
     * 获取主键
     *
     * @return 主键
     */
    public DamengRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * 设置主键
     *
     * @param primaryKey 主键
     */
    public void setPrimaryKey(DamengRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
