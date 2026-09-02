package cn.oyzh.easyshell.query.mongo;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.util.mongo.ShellMongoRecordUtil;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/19
 */
public abstract class ShellMongoQueryResult extends DBQueryResult {

    /**
     * 字段列表
     */
    protected MongoColumns columns;

    /**
     * 行列表
     */
    protected List<MongoRecord> records;

    @Override
    public int getCount() {
        return this.records == null ? 0 : this.records.size();
    }

    public void parseResult(List<MongoRecord> records) {
        this.records = records;
        this.columns = ShellMongoRecordUtil.columns(records);
    }

    public String dbName() {
        if (this.columns != null) {
            for (MongoColumn column : this.columns) {
                return column.getDbName();
            }
        }
        return null;
    }

    public String collectionName() {
        if (this.columns != null) {
            for (MongoColumn column : this.columns) {
                return column.getCollectionName();
            }
        }
        return null;
    }

    public MongoColumn getPrimaryKey() {
        if (this.columns != null) {
            for (MongoColumn column : this.columns) {
                if (column.is_id()) {
                    return column;
                }
            }
        }
        return null;
    }

    public boolean isUpdatable() {
        if (this.columns != null) {
            for (MongoColumn column : this.columns) {
                if (column.is_id()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<MongoColumn> columnList() {
        if (this.columns == null) {
            return Collections.emptyList();
        }
        return this.columns;
    }

    public MongoColumns getColumns() {
        return columns;
    }

    public void setColumns(MongoColumns columns) {
        this.columns = columns;
    }

    public List<MongoRecord> getRecords() {
        return records;
    }

    public void setRecords(List<MongoRecord> records) {
        this.records = records;
    }
}
