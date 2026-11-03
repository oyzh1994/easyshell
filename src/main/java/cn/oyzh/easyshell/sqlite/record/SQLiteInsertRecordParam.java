package cn.oyzh.easyshell.sqlite.record;

import cn.oyzh.fx.db.DBRecordData;

/**
 * SQLite新增记录参数
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteInsertRecordParam {

    /**
     * 库名称
     */
    private String dbName = "main";

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

    public SQLiteRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(SQLiteRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
