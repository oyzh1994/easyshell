package cn.oyzh.easyshell.sqlite.record;

import cn.oyzh.fx.db.DBRecordData;

/**
 * SQLite更新记录参数
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteUpdateRecordParam {

    /**
     * 库名称
     */
    private String dbName = "main";

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
    private SQLiteRecordPrimaryKey primaryKey;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public DBRecordData getRecord() {
        return record;
    }

    public void setRecord(DBRecordData record) {
        this.record = record;
    }

    public DBRecordData getUpdateRecord() {
        return updateRecord;
    }

    public void setUpdateRecord(DBRecordData updateRecord) {
        this.updateRecord = updateRecord;
    }

    public SQLiteRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(SQLiteRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
