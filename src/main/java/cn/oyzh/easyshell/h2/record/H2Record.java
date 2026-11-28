package cn.oyzh.easyshell.h2.record;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBRecord;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.DBRecordProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * H2记录
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2Record extends DBObject implements DBRecord, ObjectCopier<H2Record> {

    public static final String NULL_PROMPT = "(Null)";

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
    private H2Columns columns;

    /**
     * 记录属性集合
     */
    private HashMap<String, H2RecordProperty> properties = new HashMap<>();

    public H2Record(H2Columns columns) {
        this(columns, false);
    }

    public H2Record(List<H2Column> columns) {
        this(new H2Columns(columns), false);
    }

    public H2Record(List<H2Column> columns, boolean readonly) {
        this(new H2Columns(columns), readonly);
    }

    public H2Record(H2Columns columns, boolean readonly) {
        this.columns = columns;
        this.readonly = readonly;
    }

    /**
     * 构造记录对象
     *
     * @param columns 字段列表
     * @param record  记录数据
     * @return 记录对象
     */
    public static H2Record of(List<H2Column> columns, Map<String, Object> record) {
        return of(columns, record, false);
    }

    /**
     * 构造记录对象
     *
     * @param columns  字段列表
     * @param record   记录数据
     * @param readonly 是否只读
     * @return 记录对象
     */
    public static H2Record of(List<H2Column> columns, Map<String, Object> record, boolean readonly) {
        H2Record result = new H2Record(columns, readonly);
        for (H2Column column : columns) {
            Object value = record == null ? null : record.get(column.getName());
            result.putValue(column, value);
        }
        return result;
    }

    public H2Columns getColumns() {
        return this.columns;
    }

    public H2RecordProperty putValue(String column, Object value) {
        H2Column dbColumn = this.columns.column(column);
        if (dbColumn == null) {
            dbColumn = new H2Column(column);
        }
        return this.putValue(dbColumn, value);
    }

    public H2RecordProperty putValue(H2Column column, Object value) {
        H2RecordProperty property = this.properties.get(column.getName());
        if (property == null) {
            property = new H2RecordProperty(this, column, value, this.readonly);
            property.changedProperty().addListener((observable, oldValue, newValue) -> this.updateStatus());
            this.properties.put(column.getName(), property);
        } else {
            property.setValue(value);
        }
        return property;
    }

    public void setValue(H2Column column, Object value) {
        this.putValue(column, value);
    }

    public Object getValue(H2Column column) {
        DBRecordProperty property = this.getProperty(column.getName());
        return property == null ? null : property.get();
    }

    public boolean hasValue(H2Column column) {
        return this.properties.containsKey(column.getName());
    }

    public boolean isNull(H2Column column) {
        return this.hasValue(column) && this.getValue(column) == null;
    }

    public Map<String, Object> getValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, H2RecordProperty> entry : this.properties.entrySet()) {
            values.put(entry.getKey(), entry.getValue().get());
        }
        return values;
    }

    public Map<String, Object> getOriginalValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, H2RecordProperty> entry : this.properties.entrySet()) {
            values.put(entry.getKey(), entry.getValue().getOriginal());
        }
        return values;
    }

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
        for (H2RecordProperty property : this.properties.values()) {
            if (property.isChanged()) {
                return true;
            }
        }
        return false;
    }

//    @Override
//    public void clearStatus() {
//        try {
//            for (H2RecordProperty property : this.properties.values()) {
//                property.updateOriginal();
//                property.setChanged(false);
//            }
//        } finally {
//            super.clearStatus();
//        }
//    }

    @Override
    public void discard() {
        for (H2RecordProperty property : this.properties.values()) {
            property.discard();
        }
        super.clearStatus();
    }

    @Override
    public void copy(H2Record record) {
        if (record != null) {
            for (String column : record.columns()) {
                this.putValue(column, record.getValue(column));
            }
        }
    }

    @Override
    public DBRecordData getRecordData() {
        DBRecordData recordData = new DBRecordData();
        for (H2Column column : this.columns) {
            if (column.isGenerated()) {
                continue;
            }
            H2RecordProperty property = this.properties.get(column.getName());
            if (property == null) {
                continue;
            }
            if (column.isAutoIncrement() && property.get() == null) {
                continue;
            }
            recordData.put(column, property.get());
        }
        return recordData;
    }

    public Map<String, Object> toMap() {
        return this.getValues();
    }

    @Override
    public boolean isEditable() {
        return this.editable;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public HashMap<String, H2RecordProperty> getProperties() {
        return this.properties;
    }

    @Override
    public void destroy() {
        if (this.properties != null) {
            this.columns = null;
            for (H2RecordProperty property : this.properties.values()) {
                property.destroy();
            }
            this.properties.clear();
            this.properties = null;
        }
    }
}
