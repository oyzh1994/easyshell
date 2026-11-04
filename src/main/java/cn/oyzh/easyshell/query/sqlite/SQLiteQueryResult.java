package cn.oyzh.easyshell.query.sqlite;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.util.Collections;
import java.util.List;

/**
 * SQLite查询结果
 *
 * @author oyzh
 * @since 2026-11-02
 */
public abstract class SQLiteQueryResult extends DBQueryResult {

    /**
     * 字段列表
     */
    protected SQLiteColumns columns;

    /**
     * 行列表
     */
    protected List<SQLiteRecord> records;

    /**
     * 是否只读
     */
    protected boolean readonly;

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
            for (SQLiteColumn column : this.columns) {
                if (StringUtil.isNotBlank(column.getDbName())) {
                    return column.getDbName();
                }
            }
        }
        return "main";
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        if (this.columns != null) {
            for (SQLiteColumn column : this.columns) {
                if (StringUtil.isNotBlank(column.getTableName())) {
                    return column.getTableName();
                }
            }
        }
        return null;
    }

    /**
     * 获取主键
     *
     * @return 主键
     */
    public SQLiteColumn getPrimaryKey() {
        if (this.columns != null) {
            for (SQLiteColumn column : this.columns) {
                if (column.isPrimaryKey()) {
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
        return !this.readonly && StringUtil.isNotBlank(this.tableName()) && this.getPrimaryKey() != null;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public List<SQLiteColumn> columnList() {
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
    public SQLiteColumns getColumns() {
        return this.columns == null ? new SQLiteColumns() : this.columns;
    }

    /**
     * 设置字段列表
     *
     * @param columns 字段列表
     */
    public void setColumns(SQLiteColumns columns) {
        this.columns = columns;
    }

    /**
     * 获取行列表
     *
     * @return 行列表
     */
    public List<SQLiteRecord> getRecords() {
        return this.records == null ? Collections.emptyList() : this.records;
    }

    /**
     * 设置行列表
     *
     * @param records 行列表
     */
    public void setRecords(List<SQLiteRecord> records) {
        this.records = records;
    }

    /**
     * 是否只读
     *
     * @return 结果
     */
    public boolean isReadonly() {
        return this.readonly;
    }
}
