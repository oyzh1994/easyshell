package cn.oyzh.easyshell.dameng.record;


/**
 * @author oyzh
 * @since 2024-09-13
 */
public class DamengUpdateRecordParam {

    private String schema;

    private String tableName;

    private DamengRecordData record;

    private DamengRecordData updateRecord;

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

    public DamengRecordData getRecord() {
        return record;
    }

    public void setRecord(DamengRecordData record) {
        this.record = record;
    }

    public DamengRecordData getUpdateRecord() {
        return updateRecord;
    }

    public void setUpdateRecord(DamengRecordData updateRecord) {
        this.updateRecord = updateRecord;
    }

    public DamengRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(DamengRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
