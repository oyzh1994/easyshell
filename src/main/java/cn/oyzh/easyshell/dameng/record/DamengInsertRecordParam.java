package cn.oyzh.easyshell.dameng.record;

import cn.oyzh.fx.db.DBRecordData;

/**
 * @author oyzh
 * @since 2024-09-13
 */
public class DamengInsertRecordParam {

    private String schema;

    private String tableName;

    private DBRecordData record;

    private DamengRecordPrimaryKey primaryKey;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
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

    public DamengRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(DamengRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
