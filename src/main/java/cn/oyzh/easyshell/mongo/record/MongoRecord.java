package cn.oyzh.easyshell.mongo.record;


import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.easyshell.util.mongo.ShellMongoUtil;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBRecord;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.DBRecordProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * mongodb记录
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class MongoRecord extends DBObject implements DBRecord, ObjectCopier<MongoRecord> {

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
    private MongoColumns columns;

    public MongoRecord(MongoColumns columns) {
        this(columns, false);
    }

    public MongoRecord(List<MongoColumn> columns) {
        this(new MongoColumns(columns), false);
    }

    public MongoRecord(List<MongoColumn> columns, boolean readonly) {
        this(new MongoColumns(columns), readonly);
    }

    public MongoRecord(MongoColumns columns, boolean readonly) {
        this.columns = columns;
        this.readonly = readonly;
    }

    public MongoColumns getColumns() {
        return columns;
    }

    /**
     * 数据
     */
    private HashMap<String, MongoRecordProperty> properties = new HashMap<>();

    /**
     * 添加数据
     *
     * @param column 字段名
     * @param value  值
     * @return 数据属性
     */
    public MongoRecordProperty putValue(String column, Object value) {
        MongoRecordProperty property = (MongoRecordProperty) this.getProperty(column);
        if (property == null) {
            property = putValue(new MongoColumn(column), value);
        } else {
            property.set(value);
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
    public MongoRecordProperty putValue(MongoColumn column, Object value) {
        MongoRecordProperty property = (MongoRecordProperty) this.getProperty(column.getName());
        if (property == null) {
            property = new MongoRecordProperty(this, column, value, this.readonly);
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
        for (MongoRecordProperty property : this.properties.values()) {
            if (property.isChanged()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearStatus() {
        for (MongoRecordProperty property : this.properties.values()) {
            property.setChanged(false);
            property.updateOriginal();
        }
        super.clearStatus();
    }

    @Override
    public void discard() throws Exception {
        for (MongoRecordProperty property : this.properties.values()) {
            property.discard();
        }
        super.clearStatus();
    }

    @Override
    public void copy(MongoRecord record) {
        if (record == null) {
            return;
        }
        List<MongoColumn> delList = new ArrayList<>();
        if (record.columns != null) {
            List<MongoColumn> addList = new ArrayList<>();
            for (MongoColumn column : record.columns) {
                if (column.is_id()) {
                    continue;
                }
                MongoColumn mongoColumn = this.column(column.getName());
                if (mongoColumn == null) {
                    addList.add(column);
                } else {
                    mongoColumn.copy(column);
                }
            }
            for (MongoColumn column : this.columns) {
                if (column.is_id()) {
                    continue;
                }
                Optional<MongoColumn> optional = record.columns.stream()
                        .filter(f -> StringUtil.equals(column.getName(), f.getName()))
                        .findAny();
                if (optional.isEmpty()) {
                    delList.add(column);
                }
            }
            this.columns.addAll(addList);
            this.columns.removeAll(delList);
        }
        if (record.properties != null && record.columns != null) {
            for (MongoColumn column : record.columns) {
                if (column.is_id()) {
                    continue;
                }
                Object value = record.getValue(column.getName());
                this.putValue(column, value);
            }
        }
        if (!delList.isEmpty()) {
            this.setChanged(true);
            for (MongoColumn column : delList) {
                MongoRecordProperty property = (MongoRecordProperty) this.getProperty(column.getName());
                if (property != null) {
                    property.set(null);
                    this.removeProperty(column.getName());
                }
            }
        }
    }

    /**
     * 纠正字段
     *
     * @param columns 字段列表
     */
    public void correctColumns(MongoColumns columns) {
        if (columns != null) {
            List<MongoColumn> addList = new ArrayList<>();
            for (MongoColumn column : columns) {
                if (column.is_id()) {
                    continue;
                }
                MongoColumn mongoColumn = this.column(column.getName());
                if (mongoColumn == null) {
                    addList.add(column);
                }
            }
            this.columns.addAll(addList);
            for (MongoColumn column : addList) {
                this.putValue(column, null);
            }
        }
    }

    @Override
    public DBRecordData getRecordData() {
       throw new UnsupportedOperationException();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, MongoRecordProperty> value : this.properties.entrySet()) {
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

    public void set_id(Object _id) {
        this.putValue(ShellMongoUtil.ID, _id);
    }

    public MongoColumn column(String columnName) {
        if (this.columns == null) {
            return null;
        }
        return this.columns.column(columnName);
    }

    public MongoColumn _idColumn() {
        return this.column(ShellMongoUtil.ID);
    }

    public Object _idValue() {
        MongoRecordProperty property = (MongoRecordProperty) this.getProperty(ShellMongoUtil.ID);
        if (property == null) {
            return null;
        }
        return property.getOriginal();
    }

    @Override
    public void destroy() {
        if (this.properties != null) {
            this.columns = null;
            for (MongoRecordProperty property : this.properties.values()) {
                property.destroy();
            }
            this.properties.clear();
            this.properties = null;
        }
    }
}
