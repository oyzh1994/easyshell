package cn.oyzh.easyshell.query.dameng;

import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.util.Collections;
import java.util.List;

/**
 * 达梦查询结果
 *
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

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        if (this.columns != null) {
            for (DamengColumn column : this.columns) {
                return column.getSchema();
            }
        }
        return null;
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        if (this.columns != null) {
            for (DamengColumn column : this.columns) {
                return column.getTableName();
            }
        }
        return null;
    }

    /**
     * 获取主键
     *
     * @return 主键
     */
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

    /**
     * 是否可更新
     *
     * @return 结果
     */
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

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public List<DamengColumn> columnList() {
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
    public DamengColumns getColumns() {
        return columns;
    }

    /**
     * 设置字段列表
     *
     * @param columns 字段列表
     */
    public void setColumns(DamengColumns columns) {
        this.columns = columns;
    }

    /**
     * 获取行列表
     *
     * @return 行列表
     */
    public List<DamengRecord> getRecords() {
        return records;
    }

    /**
     * 设置行列表
     *
     * @param records 行列表
     */
    public void setRecords(List<DamengRecord> records) {
        this.records = records;
    }
}
