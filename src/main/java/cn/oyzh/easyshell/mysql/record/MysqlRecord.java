package cn.oyzh.easyshell.mysql.record;


import cn.oyzh.common.object.Destroyable;
import cn.oyzh.easyshell.db.DBObjectStatus;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * db记录
 *
 * @author oyzh
 * @since 2023/12/20
 */
public class MysqlRecord extends DBObjectStatus implements Destroyable {

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
    private MysqlColumns columns;

    public MysqlRecord(MysqlColumns columns) {
        this(columns, false);
    }

    public MysqlRecord(List<MysqlColumn> columns) {
        this(new MysqlColumns(columns), false);
    }

    public MysqlRecord(List<MysqlColumn> columns, boolean readonly) {
        this(new MysqlColumns(columns), readonly);
    }

    public MysqlRecord(MysqlColumns columns, boolean readonly) {
        this.columns = columns;
        this.readonly = readonly;
    }

    public MysqlColumns getColumns() {
        return columns;
    }

    /**
     * 数据
     */
    private HashMap<String, MysqlRecordProperty> properties = new HashMap<>();

    /**
     * 添加数据
     *
     * @param column 字段名
     * @param value  值
     * @return 数据属性
     */
    public MysqlRecordProperty putValue(String column, Object value) {
        MysqlRecordProperty property = this.getProperty(column);
        if (property == null) {
            property = putValue(new MysqlColumn(column), value);
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
    public MysqlRecordProperty putValue(MysqlColumn column, Object value) {
        MysqlRecordProperty property = this.getProperty(column.getName());
        if (property == null) {
            property = new MysqlRecordProperty(this, column, value, this.readonly);
            property.changedProperty().addListener((observable, oldValue, newValue) -> this.updateStatus());
            this.properties.put(column.getName(), property);
        } else {
            property.setValue(value);
        }
        return property;
    }

    /**
     * 获取数据
     *
     * @param column 字段名
     * @return 数据
     */
    public Object getValue(String column) {
        MysqlRecordProperty property = this.getProperty(column);
        return property == null ? null : property.get();
    }

    /**
     * 获取原始数据
     *
     * @param column 字段名
     * @return 原始数据
     */
    public Object getOriginal(String column) {
        MysqlRecordProperty property = this.getProperty(column);
        return property == null ? null : property.getOriginal();
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public Set<String> columns() {
        return this.properties.keySet();
    }

    /**
     * 获取记录属性
     *
     * @param key 键
     * @return 属性
     */
    public MysqlRecordProperty getProperty(String key) {
        return this.properties.get(key);
    }

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 属性
     */
    public boolean hasProperty(MysqlRecordProperty recordProperty) {
        return this.properties.containsValue(recordProperty);
    }

    /**
     * 清除数据
     */
    public void clear() {
        this.properties.clear();
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
        for (MysqlRecordProperty property : this.properties.values()) {
            if (property.isChanged()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearStatus() {
        for (MysqlRecordProperty property : this.properties.values()) {
            property.setChanged(false);
            property.updateOriginal();
        }
        super.clearStatus();
    }

    /**
     * 抛弃变更
     */
    public void discard() throws Exception {
        for (MysqlRecordProperty property : this.properties.values()) {
            property.discard();
        }
        super.clearStatus();
    }

    public void copy(MysqlRecord record) {
        if (record != null) {
            for (String column : record.columns()) {
                Object value = record.getValue(column);
                if (value != null) {
                    this.putValue(column, value);
                }
            }
        }
    }

    /**
     * 获取记录数据
     *
     * @return 结果
     */
    public MysqlRecordData getRecordData() {
        MysqlRecordData recordData = new MysqlRecordData();
        for (String column : this.columns()) {
            MysqlRecordProperty property = this.getProperty(column);
            if (property != null) {
                Object value = property.get();
                if (value != null) {
                    recordData.put(property.getColumn(), value);
                }
            }
        }
        return recordData;
    }

    /**
     * 获取变更后记录数据
     *
     * @return 结果
     */
    public MysqlRecordData getChangedRecordData() {
        MysqlRecordData recordData = new MysqlRecordData();
        for (String column : this.columns()) {
            MysqlRecordProperty property = this.getProperty(column);
            if (property != null && property.isChanged()) {
                recordData.put(property.getColumn(), property.get());
            }
        }
        return recordData;
    }

    /**
     * 获取原始记录数据
     *
     * @return 结果
     */
    public MysqlRecordData getOriginalRecordData() {
        MysqlRecordData recordData = new MysqlRecordData();
        for (String column : this.columns()) {
            MysqlRecordProperty property = this.getProperty(column);
            if (property != null) {
                Object val = property.getOriginal();
                // if (val != null) {
                recordData.put(property.getColumn(), val);
                // }
            }
        }
        return recordData;
    }


    /**
     * 字段是否变更
     *
     * @param column 字段
     * @return 结果
     */
    public boolean isColumnChanged(String column) {
        MysqlRecordProperty property = this.getProperty(column);
        return property != null && property.isChanged();
    }

    /**
     * 转换为map集合
     *
     * @return map集合
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, MysqlRecordProperty> value : this.properties.entrySet()) {
            map.put(value.getKey(), value.getValue().get());
        }
        return map;
    }

    @Override
    public synchronized void destroy() {
        if (this.properties != null) {
            this.columns = null;
            for (MysqlRecordProperty property : this.properties.values()) {
                property.destroy();
            }
            this.properties.clear();
            this.properties = null;
        }
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
