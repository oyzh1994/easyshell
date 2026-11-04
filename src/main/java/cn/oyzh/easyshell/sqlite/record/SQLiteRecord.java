package cn.oyzh.easyshell.sqlite.record;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBRecord;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.DBRecordProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite记录
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteRecord extends DBObject implements DBRecord, ObjectCopier<SQLiteRecord> {

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
    private SQLiteColumns columns;

    /**
     * 记录属性集合
     */
    private HashMap<String, SQLiteRecordProperty> properties = new HashMap<>();

    public SQLiteRecord(SQLiteColumns columns) {
        this(columns, false);
    }

    public SQLiteRecord(List<SQLiteColumn> columns) {
        this(new SQLiteColumns(columns), false);
    }

    public SQLiteRecord(List<SQLiteColumn> columns, boolean readonly) {
        this(new SQLiteColumns(columns), readonly);
    }

    public SQLiteRecord(SQLiteColumns columns, boolean readonly) {
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
    public static SQLiteRecord of(List<SQLiteColumn> columns, Map<String, Object> record) {
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
    public static SQLiteRecord of(List<SQLiteColumn> columns, Map<String, Object> record, boolean readonly) {
        SQLiteRecord result = new SQLiteRecord(columns, readonly);
        for (SQLiteColumn column : columns) {
            Object value = record == null ? null : record.get(column.getName());
            result.putValue(column, value);
        }
        return result;
    }

    public SQLiteColumns getColumns() {
        return this.columns;
    }

    public SQLiteRecordProperty putValue(String column, Object value) {
        SQLiteColumn dbColumn = this.columns.column(column);
        if (dbColumn == null) {
            dbColumn = new SQLiteColumn(column);
        }
        return this.putValue(dbColumn, value);
    }

    public SQLiteRecordProperty putValue(SQLiteColumn column, Object value) {
        SQLiteRecordProperty property = this.properties.get(column.getName());
        if (property == null) {
            property = new SQLiteRecordProperty(this, column, value, this.readonly);
            property.changedProperty().addListener((observable, oldValue, newValue) -> this.updateStatus());
            this.properties.put(column.getName(), property);
        } else {
            property.setValue(value);
        }
        return property;
    }

    public void setValue(SQLiteColumn column, Object value) {
        this.putValue(column, value);
    }

    public Object getValue(SQLiteColumn column) {
        DBRecordProperty property = this.getProperty(column.getName());
        return property == null ? null : property.get();
    }

    public boolean hasValue(SQLiteColumn column) {
        return this.properties.containsKey(column.getName());
    }

    public boolean isNull(SQLiteColumn column) {
        return this.hasValue(column) && this.getValue(column) == null;
    }

    public Map<String, Object> getValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, SQLiteRecordProperty> entry : this.properties.entrySet()) {
            values.put(entry.getKey(), entry.getValue().get());
        }
        return values;
    }

    public Map<String, Object> getOriginalValues() {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, SQLiteRecordProperty> entry : this.properties.entrySet()) {
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
        for (SQLiteRecordProperty property : this.properties.values()) {
            if (property.isChanged()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearStatus() {
        for (SQLiteRecordProperty property : this.properties.values()) {
            property.setChanged(false);
            property.updateOriginal();
        }
        super.clearStatus();
    }

    @Override
    public void discard() {
        for (SQLiteRecordProperty property : this.properties.values()) {
            property.discard();
        }
        super.clearStatus();
    }

    @Override
    public void copy(SQLiteRecord record) {
        if (record != null) {
            for (String column : record.columns()) {
                this.putValue(column, record.getValue(column));
            }
        }
    }

    @Override
    public DBRecordData getRecordData() {
        DBRecordData recordData = new DBRecordData();
        for (SQLiteColumn column : this.columns) {
            if (column.isGenerated()) {
                continue;
            }
            SQLiteRecordProperty property = this.properties.get(column.getName());
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
    public HashMap<String, SQLiteRecordProperty> getProperties() {
        return this.properties;
    }

    @Override
    public void destroy() {
        if (this.properties != null) {
            this.columns = null;
            for (SQLiteRecordProperty property : this.properties.values()) {
                property.destroy();
            }
            this.properties.clear();
            this.properties = null;
        }
    }
}
