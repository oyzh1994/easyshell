package cn.oyzh.easyshell.query.mariadb;

import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.util.Collections;
import java.util.List;

/**
 * MariaDB查询结果
 *
 * @author oyzh
 * @since 2026-10-09
 */
public abstract class ShellMariadbQueryResult extends DBQueryResult {

    /**
     * 字段列表
     */
    protected MariadbColumns columns;

    /**
     * 行列表
     */
    protected List<MariadbRecord> records;

    @Override
    public int getCount() {
        return this.records == null ? 0 : this.records.size();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        if (this.columns != null) {
            for (MariadbColumn column : this.columns) {
                return column.getDbName();
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
            for (MariadbColumn column : this.columns) {
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
    public MariadbColumn getPrimaryKey() {
        if (this.columns != null) {
            for (MariadbColumn column : this.columns) {
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
            for (MariadbColumn column : this.columns) {
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
    public List<MariadbColumn> columnList() {
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
    public MariadbColumns getColumns() {
        return columns;
    }

    /**
     * 设置字段列表
     *
     * @param columns 字段列表
     */
    public void setColumns(MariadbColumns columns) {
        this.columns = columns;
    }

    /**
     * 获取行列表
     *
     * @return 行列表
     */
    public List<MariadbRecord> getRecords() {
        return records;
    }

    /**
     * 设置行列表
     *
     * @param records 行列表
     */
    public void setRecords(List<MariadbRecord> records) {
        this.records = records;
    }
}
