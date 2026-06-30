package cn.oyzh.easyshell.query.mongo;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mongo.MongoColumn;
import cn.oyzh.easyshell.mongo.MongoColumns;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.util.mongo.MongoRecordUtil;

import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/19
 */
public abstract class MongoQueryResult {

    /**
     * 脚本
     */
    protected String script;

    /**
     * 耗时，微妙
     */
    protected long used;

    /**
     * 消息
     */
    protected String msg;

    /**
     * 变更总数
     */
    protected long updateCount;

    /**
     * 是否成功
     */
    protected boolean success;

    /**
     * 字段列表
     */
    protected MongoColumns columns;

    /**
     * 行列表
     */
    protected List<MongoRecord> records;

    public boolean hasResult() {
        if (CollectionUtil.isNotEmpty(this.records)) {
            return true;
        }
        return this.columns == null || this.columns.isEmpty();
    }

    public void parseResult(List<MongoRecord> records) {
        this.records = records;
        this.columns = MongoRecordUtil.columns(records);
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

    public int getCount() {
        return this.records == null ? 0 : this.records.size();
    }

    public long getUsedMs() {
        return this.used / 1_000_000L;
    }

    public List<MongoColumn> columnList() {
        if (this.columns == null) {
            return Collections.emptyList();
        }
        return this.columns;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(long updateCount) {
        this.updateCount = updateCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
