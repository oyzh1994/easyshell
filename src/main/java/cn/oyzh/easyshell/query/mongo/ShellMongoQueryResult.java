package cn.oyzh.easyshell.query.mongo;

import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.util.mongo.ShellMongoRecordUtil;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.util.Collections;
import java.util.List;

/**
 * mongo查询结果
 *
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

    /**
     * 解析结果
     *
     * @param records 行列表
     */
    public void parseResult(List<MongoRecord> records) {
        this.records = records;
        this.columns = ShellMongoRecordUtil.columns(records);
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        if (this.columns != null) {
            for (MongoColumn column : this.columns) {
                return column.getDbName();
            }
        }
        return null;
    }

    /**
     * 获取集合名称
     *
     * @return 集合名称
     */
    public String collectionName() {
        if (this.columns != null) {
            for (MongoColumn column : this.columns) {
                return column.getCollectionName();
            }
        }
        return null;
    }

    /**
     * 获取主键
     *
     * @return 主键
     */
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

    /**
     * 是否可更新
     *
     * @return 结果
     */
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

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public List<MongoColumn> columnList() {
        if (this.columns == null) {
            return Collections.emptyList();
        }
        return this.columns;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public MongoColumns getColumns() {
        return columns;
    }

    /**
     * 设置字段列表
     *
     * @param columns 字段列表
     */
    public void setColumns(MongoColumns columns) {
        this.columns = columns;
    }

    /**
     * 获取行列表
     *
     * @return 行列表
     */
    public List<MongoRecord> getRecords() {
        return records;
    }

    /**
     * 设置行列表
     *
     * @param records 行列表
     */
    public void setRecords(List<MongoRecord> records) {
        this.records = records;
    }
}
