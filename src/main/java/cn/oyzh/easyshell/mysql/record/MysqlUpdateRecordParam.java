package cn.oyzh.easyshell.mysql.record;


import cn.oyzh.fx.db.DBRecordData;

/**
 * @author oyzh
 * @since 2024-09-13
 */
public class MysqlUpdateRecordParam {

    private String dbName;

    // private String schema;

    private String tableName;

    private DBRecordData record;

    private DBRecordData updateRecord;

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

    public MysqlRecordPrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(MysqlRecordPrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
