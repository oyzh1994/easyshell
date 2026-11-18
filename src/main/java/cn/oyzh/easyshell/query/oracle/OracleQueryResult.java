package cn.oyzh.easyshell.query.oracle;

import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.util.Collections;
import java.util.List;

/**
 * Oracle查询结果
 *
 * @author oyzh
 * @since 2025-11-06
 */
public abstract class OracleQueryResult extends DBQueryResult {

    /**
     * 字段列表
     */
    protected OracleColumns columns;

    /**
     * 行列表
     */
    protected List<OracleRecord> records;

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
            for (OracleColumn column : this.columns) {
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
            for (OracleColumn column : this.columns) {
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
    public OracleColumn getPrimaryKey() {
        if (this.columns != null) {
            for (OracleColumn column : this.columns) {
                if (column.isPrimaryKey() && column.isAutoIncrement()) {
                    return column;
                }
            }
            for (OracleColumn column : this.columns) {
                if (column.isPrimaryKey()) {
                    return column;
                }
            }
            for (OracleColumn column : this.columns) {
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
        return this.getPrimaryKey() != null;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public List<OracleColumn> columnList() {
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
    public OracleColumns getColumns() {
        return columns;
    }

    /**
     * 设置字段列表
     *
     * @param columns 字段列表
     */
    public void setColumns(OracleColumns columns) {
        this.columns = columns;
    }

    /**
     * 获取行列表
     *
     * @return 行列表
     */
    public List<OracleRecord> getRecords() {
        return records;
    }

    /**
     * 设置行列表
     *
     * @param records 行列表
     */
    public void setRecords(List<OracleRecord> records) {
        this.records = records;
    }
}
