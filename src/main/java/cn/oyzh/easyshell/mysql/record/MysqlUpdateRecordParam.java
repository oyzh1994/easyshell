package cn.oyzh.easyshell.mysql.record;


/**
 * @author oyzh
 * @since 2024-09-13
 */
public class MysqlUpdateRecordParam {

    private String dbName;

    // private String schema;

    private String tableName;

    private MysqlRecordData record;

    private MysqlRecordData updateRecord;

    private MysqlRecordPrimaryKey primaryKey;

    public String getDbName() {
        return dbName;
    }

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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public MysqlRecordData getRecord() {
        return record;
    }

    public void setRecord(MysqlRecordData record) {
        this.record = record;
    }

    public MysqlRecordData getUpdateRecord() {
        return updateRecord;
    }

    public void setUpdateRecord(MysqlRecordData updateRecord) {
        this.updateRecord = updateRecord;
    }

    public MysqlRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(MysqlRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
