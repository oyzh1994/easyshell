package cn.oyzh.easyshell.dameng.record;

import cn.oyzh.easyshell.dameng.column.DamengColumn;

import java.util.Objects;

/**
 * @author oyzh
 * @since 2023/12/29
 */
public class DamengRecordPrimaryKey {

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
    private DamengColumn column;

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

    public void init(DamengColumn column, DamengRecord record) {
        this.column = column;
        this.columnName = column.getName();
        this.autoIncrement = column.isAutoIncrement();
        this.data = record.getValue(this.columnName);
        this.originalData = record.getOriginal(this.columnName);
    }

    public Object data() {
        if (this.data != null) {
            return this.data;
        }
        return this.returnData;
    }

    public Object originalData() {
        if (this.originalData != null) {
            return this.originalData;
        }
        return this.data;
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

    public DamengColumn getColumn() {
        return column;
    }
}
