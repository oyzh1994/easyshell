package cn.oyzh.easyshell.mysql.query;

import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/19
 */
public abstract class ShellMysqlQueryResult extends DBQueryResult {

    /**
     * 字段列表
     */
    protected MysqlColumns columns;

    /**
     * 行列表
     */
    protected List<MysqlRecord> records;

    @Override
    public int getCount() {
        return this.records == null ? 0 : this.records.size();
    }

    public String dbName() {
        if (this.columns != null) {
            for (MysqlColumn column : this.columns) {
                return column.getDbName();
            }
        }
        return null;
    }

    public String tableName() {
        if (this.columns != null) {
            for (MysqlColumn column : this.columns) {
                return column.getTableName();
            }
        }
        return null;
    }

    public MysqlColumn getPrimaryKey() {
        if (this.columns != null) {
            for (MysqlColumn column : this.columns) {
                if (column.isAutoIncrement()) {
                    return column;
                }
            }
        }
        return null;
    }

    public boolean isUpdatable() {
        if (this.columns != null) {
            for (MysqlColumn column : this.columns) {
                if (column.isAutoIncrement()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<MysqlColumn> columnList() {
        if (this.columns == null) {
            return Collections.emptyList();
        }
        return this.columns;
    }

    public MysqlColumns getColumns() {
        return columns;
    }

    public void setColumns(MysqlColumns columns) {
        this.columns = columns;
    }

    public List<MysqlRecord> getRecords() {
        return records;
    }

    public void setRecords(List<MysqlRecord> records) {
        this.records = records;
    }
}
