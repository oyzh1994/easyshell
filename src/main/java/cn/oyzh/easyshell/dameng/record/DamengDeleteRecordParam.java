package cn.oyzh.easyshell.dameng.record;


import cn.oyzh.easyshell.dameng.record.DamengRecordData;
import cn.oyzh.easyshell.dameng.record.DamengRecordPrimaryKey;

/**
 * @author oyzh
 * @since 2024-09-13
 */
public class DamengDeleteRecordParam {

    private String schema;

    private String tableName;

    private DamengRecordData record;

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

    public DamengRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(DamengRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
