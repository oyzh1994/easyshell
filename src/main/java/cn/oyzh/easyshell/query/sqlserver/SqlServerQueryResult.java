package cn.oyzh.easyshell.query.sqlserver;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * SqlServer查询结果
 *
 * @author oyzh
 * @since 2025-11-06
 */
public abstract class SqlServerQueryResult extends DBQueryResult {

    /**
     * 字段列表
     */
    protected SqlServerColumns columns;

    /**
     * 行列表
     */
    protected List<SqlServerRecord> records;

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
            for (SqlServerColumn column : this.columns) {
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
            for (SqlServerColumn column : this.columns) {
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
    public SqlServerColumn getPrimaryKey() {
        if (this.columns != null) {
            for (SqlServerColumn column : this.columns) {
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
            for (SqlServerColumn column : this.columns) {
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
    public List<SqlServerColumn> columnList() {
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
    public SqlServerColumns getColumns() {
        return columns;
    }

    /**
     * 设置字段列表
     *
     * @param columns 字段列表
     */
    public void setColumns(SqlServerColumns columns) {
        this.columns = columns;
    }

    /**
     * 回填字段所属模式
     * SqlServer的ResultSet元数据通常不返回schema，使用执行SQL时的当前模式补充
     *
     * @param schema 模式名称
     */
    public void fillSchema(String schema) {
        if (StringUtil.isBlank(schema) || this.columns == null) {
            return;
        }
        for (SqlServerColumn column : this.columns) {
            if (StringUtil.isBlank(column.getSchema())) {
                column.setSchema(schema);
            }
        }
    }

    /**
     * 获取行列表
     *
     * @return 行列表
     */
    public List<SqlServerRecord> getRecords() {
        return records;
    }

    /**
     * 设置行列表
     *
     * @param records 行列表
     */
    public void setRecords(List<SqlServerRecord> records) {
        this.records = records;
    }

    /**
     * 读取字段值
     * <p>
     * 带时区的时间类型通过字符串读取，避免丢失时区信息。
     *
     * @param resultSet 结果集
     * @param column    字段
     * @param index     索引
     * @return 字段值
     * @throws SQLException 异常
     */
    protected Object getValue(ResultSet resultSet, SqlServerColumn column, int index) throws SQLException {
        String type = column == null ? null : column.getType();
        if (StringUtil.equalsIgnoreCase(type, "DATETIMEOFFSET")) {
            return resultSet.getString(index);
        }
        return resultSet.getObject(index);
    }

    /**
     * 解析字段值，转换为便于界面展示的形式
     * <p>
     * 二进制值保留byte[]供编辑器识别；SQL Server CLR类型转换为可显示字符串。
     *
     * @param value 字段值
     * @return 解析后的字段值
     * @throws SQLException 异常
     */
    protected Object parseValue(Object value) throws SQLException {
        if (value == null) {
            return null;
        }
        // hierarchyid、geometry、geography等SQL Server CLR类型统一显示为字符串
        String className = value.getClass().getName();
        if (className.startsWith("microsoft.sql.")
                || className.startsWith("com.microsoft.sqlserver.jdbc.")) {
            return value.toString();
        }
        return value;
    }

}
