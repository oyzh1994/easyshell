package cn.oyzh.easyshell.sqlite.record;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;

import java.util.Objects;

/**
 * SQLite记录主键
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteRecordPrimaryKey {

    /**
     * 当前数据
     */
    private Object data;

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 字段
     */
    private SQLiteColumn column;

    /**
     * 自动递增的返回值
     */
    private Object returnData;

    /**
     * 编辑前的原始数据
     */
    private Object originalData;

    /**
     * 是否自动递增
     */
    private boolean autoIncrement;

    /**
     * 初始化主键信息
     *
     * @param column 字段
     * @param record 记录
     */
    public void init(SQLiteColumn column, SQLiteRecord record) {
        this.column = column;
        this.columnName = column.getName();
        this.autoIncrement = column.isAutoIncrement();
        this.data = record.getValue(column);
        this.originalData = record.getOriginal(this.columnName);
    }

    public Object data() {
        return this.data != null ? this.data : this.returnData;
    }

    public Object originalData() {
        return this.originalData != null ? this.originalData : this.data;
    }

    public boolean shouldReturnData() {
        return this.data == null && this.autoIncrement;
    }

    public boolean isChanged() {
        if (this.originalData == null) {
            return false;
        }
        return !Objects.equals(this.originalData, this.data);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Object getReturnData() {
        return returnData;
    }

    public void setReturnData(Object returnData) {
        this.returnData = returnData;
    }

    public Object getOriginalData() {
        return originalData;
    }

    public void setOriginalData(Object originalData) {
        this.originalData = originalData;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public SQLiteColumn getColumn() {
        return column;
    }
}
