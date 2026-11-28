package cn.oyzh.easyshell.h2.record;

import cn.oyzh.fx.db.DBRecordData;

/**
 * H2新增记录参数
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2InsertRecordParam {

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
    private H2RecordPrimaryKey primaryKey;

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

    public H2RecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(H2RecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
