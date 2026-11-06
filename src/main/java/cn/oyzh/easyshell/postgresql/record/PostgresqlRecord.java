package cn.oyzh.easyshell.postgresql.record;


import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBRecord;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.DBRecordProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL记录
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class PostgresqlRecord extends DBObject implements DBRecord, ObjectCopier<PostgresqlRecord> {

    /**
     * 是否只读
     */
    private final boolean readonly;

    /**
     * 是否可编辑
     */
    private boolean editable;

    /**
     * 字段列表
     */
    private PostgresqlColumns columns;

    /**
     * 构造记录
     *
     * @param columns 字段列表
     */
    public PostgresqlRecord(PostgresqlColumns columns) {
        this(columns, false);
    }

    /**
     * 构造记录
     *
     * @param columns 字段列表
     */
    public PostgresqlRecord(List<PostgresqlColumn> columns) {
        this(new PostgresqlColumns(columns), false);
    }

    /**
     * 构造记录
     *
     * @param columns  字段列表
     * @param readonly 是否只读
     */
    public PostgresqlRecord(List<PostgresqlColumn> columns, boolean readonly) {
        this(new PostgresqlColumns(columns), readonly);
    }

    /**
     * 构造记录
     *
     * @param columns  字段列表
     * @param readonly 是否只读
     */
    public PostgresqlRecord(PostgresqlColumns columns, boolean readonly) {
        this.columns = columns;
        this.readonly = readonly;
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
     * 记录属性集合
     */
    private HashMap<String, PostgresqlRecordProperty> properties = new HashMap<>();

    /**
     * 添加数据
     *
     * @param column 字段名
     * @param value  值
     * @return 数据属性
     */
    public PostgresqlRecordProperty putValue(String column, Object value) {
        PostgresqlRecordProperty property = (PostgresqlRecordProperty) this.getProperty(column);
        if (property == null) {
            property = this.putValue(new PostgresqlColumn(column), value);
        } else {
            property.setValue(value);
        }
        return property;
    }

    /**
     * 添加数据
     *
     * @param column 字段
     * @param value  值
     * @return 数据属性
     */
    public PostgresqlRecordProperty putValue(PostgresqlColumn column, Object value) {
        PostgresqlRecordProperty property = (PostgresqlRecordProperty) this.getProperty(column.getName());
        if (property == null) {
            property = new PostgresqlRecordProperty(this, column, value, this.readonly);
            property.changedProperty().addListener((observable, oldValue, newValue) -> this.updateStatus());
            this.properties.put(column.getName(), property);
        } else {
            property.setValue(value);
        }
        return property;
    }

    /**
     * 更新数据
     *
     * @param rowData 新数据
     */
    public void update(Map<String, Object> rowData) {
        if (rowData != null) {
            for (Map.Entry<String, Object> entry : rowData.entrySet()) {
                this.putValue(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public boolean isChanged() {
        if (super.isChanged()) {
            return true;
        }
        for (PostgresqlRecordProperty property : this.properties.values()) {
            if (property.isChanged()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearStatus() {
        for (PostgresqlRecordProperty property : this.properties.values()) {
            property.setChanged(false);
            property.updateOriginal();
        }
        super.clearStatus();
    }

    @Override
    public void discard() throws Exception {
        for (PostgresqlRecordProperty property : this.properties.values()) {
            property.discard();
        }
        super.clearStatus();
    }

    @Override
    public void copy(PostgresqlRecord record) {
        if (record != null) {
            for (String column : record.columns()) {
                Object value = record.getValue(column);
                if (value != null) {
                    this.putValue(column, value);
                }
            }
        }
    }


    @Override
    public DBRecordData getRecordData() {
        DBRecordData recordData = new DBRecordData();
        for (PostgresqlColumn column : this.getColumns()) {
            PostgresqlRecordProperty property = (PostgresqlRecordProperty) this.getProperty(column.getName());
            if (property == null) {
                continue;
            }
            // 主键忽略
            if (column.isPrimaryKey() || column.isAutoIncrement()) {
                continue;
            }
            Object value = property.get();
            //if (value == null) {
            //    continue;
            //}
            // TODO: 时间戳处理
            if (value != null && column.supportTimestamp()
                    && StringUtil.equalsAnyIgnoreCase(value.toString(), "CURRENT_TIMESTAMP()", "CURRENT_TIMESTAMP", "NOW()")) {
                continue;
            }
            recordData.put(property.getColumn(), value);
        }
        return recordData;
    }

    /**
     * 转换为map集合
     *
     * @return map集合
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, PostgresqlRecordProperty> value : this.properties.entrySet()) {
            map.put(value.getKey(), value.getValue().get());
        }
        return map;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public HashMap<String, ? extends DBRecordProperty> getProperties() {
        return this.properties;
    }

    @Override
    public void destroy() {
        if (this.properties != null) {
            this.columns = null;
            for (PostgresqlRecordProperty property : this.properties.values()) {
                property.destroy();
            }
            this.properties.clear();
            this.properties = null;
        }
    }
}
