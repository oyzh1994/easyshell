package cn.oyzh.easyshell.query.postgresql;

import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlDataUtil;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * PostgreSQL查询结果
 *
 * @author oyzh
 * @since 2025-11-06
 */
public abstract class PostgresqlQueryResult extends DBQueryResult {

    /**
     * 字段列表
     */
    protected PostgresqlColumns columns;

    /**
     * 行列表
     */
    protected List<PostgresqlRecord> records;

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
            for (PostgresqlColumn column : this.columns) {
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
            for (PostgresqlColumn column : this.columns) {
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
    public PostgresqlColumn getPrimaryKey() {
        if (this.columns != null) {
            for (PostgresqlColumn column : this.columns) {
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
            for (PostgresqlColumn column : this.columns) {
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
    public List<PostgresqlColumn> columnList() {
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
    public PostgresqlColumns getColumns() {
        return columns;
    }

    /**
     * 设置字段列表
     *
     * @param columns 字段列表
     */
    public void setColumns(PostgresqlColumns columns) {
        this.columns = columns;
    }

    /**
     * 回填字段所属模式
     * PostgreSQL的ResultSet元数据通常不返回schema，使用执行SQL时的当前模式补充
     *
     * @param schema 模式名称
     */
    public void fillSchema(String schema) {
        if (StringUtil.isBlank(schema) || this.columns == null) {
            return;
        }
        for (PostgresqlColumn column : this.columns) {
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
    public List<PostgresqlRecord> getRecords() {
        return records;
    }

    /**
     * 设置行列表
     *
     * @param records 行列表
     */
    public void setRecords(List<PostgresqlRecord> records) {
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
    protected Object getValue(ResultSet resultSet, PostgresqlColumn column, int index) throws SQLException {
        String type = column == null ? null : column.getType();
        if (StringUtil.equalsAnyIgnoreCase(type, "timestamptz", "timestamp with time zone",
                "timetz", "time with time zone")) {
            return resultSet.getString(index);
        }
        return resultSet.getObject(index);
    }

    /**
     * 解析字段值，转换为便于界面展示的形式
     * <p>
     * JDBC扩展类型统一交由{@link ShellPostgresqlDataUtil#valueStandardization(PostgresqlColumn, Object)}
     * 处理；bytea保留字节数组以便按二进制类型渲染，其余字节数组转换为十六进制文本。
     *
     * @param column 字段
     * @param value  字段值
     * @return 解析后的字段值
     * @throws SQLException 异常
     */
    protected Object parseValue(PostgresqlColumn column, Object value) throws SQLException {
        if (value == null) {
            return null;
        }
        value = ShellPostgresqlDataUtil.valueStandardization(column, value);
        // bytea保留字节数组，由记录渲染层统一显示为二进制大小；BIT同样使用字节数组表示
        if (value instanceof byte[] bytes) {
            return column != null && (column.supportBinary() || column.supportBit())
                    ? bytes
                    : "\\x" + HexUtil.encodeHexStr(bytes, true);
        }
        return value;
    }
}
