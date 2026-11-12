package cn.oyzh.easyshell.query.postgresql;

import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.fx.db.query.DBQueryResult;

import java.sql.Array;
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
     * PostgreSQL驱动会把json/jsonb、uuid、interval等类型包装为扩展对象，
     * bytea返回字节数组，数组类型返回{@link Array}，此处统一转换为可显示的字符串。
     *
     * @param value 字段值
     * @return 解析后的字段值
     * @throws SQLException 异常
     */
    protected Object parseValue(Object value) throws SQLException {
        if (value == null) {
            return null;
        }
        // bytea显示为hex形式
        if (value instanceof byte[] bytes) {
            return "\\x" + HexUtil.encodeHexStr(bytes, true);
        }
        // 数组显示为PostgreSQL数组字面量
        if (value instanceof Array array) {
            try {
                return this.formatArray(array.getArray());
            } finally {
                array.free();
            }
        }
        // json/jsonb、uuid、interval、money等PostgreSQL扩展类型，统一显示为字符串
        if (value.getClass().getName().startsWith("org.postgresql.")) {
            return value.toString();
        }
        return value;
    }

    /**
     * 格式化数组
     *
     * @param value 数组值
     * @return 格式化后的数组
     */
    private String formatArray(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            StringBuilder builder = new StringBuilder("{");
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    builder.append(',');
                }
                builder.append(this.formatArrayElement(java.lang.reflect.Array.get(value, i)));
            }
            return builder.append('}').toString();
        }
        return this.formatArrayElement(value);
    }

    /**
     * 格式化数组元素
     *
     * @param value 元素值
     * @return 格式化后的元素
     */
    private String formatArrayElement(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof byte[] bytes) {
            return "\\x" + HexUtil.encodeHexStr(bytes, true);
        }
        if (value.getClass().isArray()) {
            return this.formatArray(value);
        }
        String str = value.toString();
        // 空字符串、NULL字面量及包含特殊字符的元素需要加双引号
        boolean quote = str.isEmpty() || str.equalsIgnoreCase("NULL");
        if (!quote) {
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c == '{' || c == '}' || c == ',' || c == '"' || c == '\\' || Character.isWhitespace(c)) {
                    quote = true;
                    break;
                }
            }
        }
        if (quote) {
            return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return str;
    }
}
