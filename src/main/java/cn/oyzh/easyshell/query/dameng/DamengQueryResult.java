package cn.oyzh.easyshell.query.dameng;

import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/19
 */
public abstract class DamengQueryResult extends DBQueryResult {

    /**
     * 字段列表
     */
    protected DamengColumns columns;

    /**
     * 行列表
     */
    protected List<DamengRecord> records;

    @Override
    public int getCount() {
        return this.records == null ? 0 : this.records.size();
    }

    public String schema() {
        if (this.columns != null) {
            for (DamengColumn column : this.columns) {
                return column.getSchema();
            }
        }
        return null;
    }

    public String tableName() {
        if (this.columns != null) {
            for (DamengColumn column : this.columns) {
                return column.getTableName();
            }
        }
        return null;
    }

    public DamengColumn getPrimaryKey() {
        if (this.columns != null) {
            for (DamengColumn column : this.columns) {
                if (column.isAutoIncrement()) {
                    return column;
                }
            }
        }
        return null;
    }

    public boolean isUpdatable() {
        if (this.columns != null) {
            for (DamengColumn column : this.columns) {
                if (column.isAutoIncrement()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<DamengColumn> columnList() {
        if (this.columns == null) {
            return Collections.emptyList();
        }
        return this.columns;
    }

    public DamengColumns getColumns() {
        return columns;
    }

    public void setColumns(DamengColumns columns) {
        this.columns = columns;
    }

    public List<DamengRecord> getRecords() {
        return records;
    }

    public void setRecords(List<DamengRecord> records) {
        this.records = records;
    }
}
