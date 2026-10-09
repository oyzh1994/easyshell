package cn.oyzh.easyshell.mariadb.record;


import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBRecordData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MariaDB记录
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbRecord extends DBObject {

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
    private MariadbColumns columns;

    /**
     * 构造记录
     *
     * @param columns 字段列表
     */
    public MariadbRecord(MariadbColumns columns) {
        this(columns, false);
    }

    /**
     * 构造记录
     *
     * @param columns 字段列表
     */
    public MariadbRecord(List<MariadbColumn> columns) {
        this(new MariadbColumns(columns), false);
    }

    /**
     * 构造记录
     *
     * @param columns  字段列表
     * @param readonly 是否只读
     */
    public MariadbRecord(List<MariadbColumn> columns, boolean readonly) {
        this(new MariadbColumns(columns), readonly);
    }

    /**
     * 构造记录
     *
     * @param columns  字段列表
     * @param readonly 是否只读
     */
    public MariadbRecord(MariadbColumns columns, boolean readonly) {
        this.columns = columns;
        this.readonly = readonly;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public MariadbColumns getColumns() {
        return columns;
    }

    /**
     * 记录属性集合
     */
    private HashMap<String, MariadbRecordProperty> properties = new HashMap<>();

    /**
     * 添加数据
     *
     * @param column 字段名
     * @param value  值
     * @return 数据属性
     */
    public MariadbRecordProperty putValue(String column, Object value) {
        MariadbRecordProperty property = this.getProperty(column);
        if (property == null) {
            property = putValue(new MariadbColumn(column), value);
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
    public MariadbRecordProperty putValue(MariadbColumn column, Object value) {
        MariadbRecordProperty property = this.getProperty(column.getName());
        if (property == null) {
            property = new MariadbRecordProperty(this, column, value, this.readonly);
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
        MariadbRecordProperty property = this.getProperty(column);
        return property == null ? null : property.get();
    }

    /**
     * 获取原始数据
     *
     * @param column 字段名
     * @return 原始数据
     */
    public Object getOriginal(String column) {
        MariadbRecordProperty property = this.getProperty(column);
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
    public MariadbRecordProperty getProperty(String key) {
        return this.properties.get(key);
    }

    /**
     * 是否存在记录属性
     *
     * @param recordProperty 记录属性
     * @return 属性
     */
    public boolean hasProperty(MariadbRecordProperty recordProperty) {
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
        for (MariadbRecordProperty property : this.properties.values()) {
            if (property.isChanged()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearStatus() {
        for (MariadbRecordProperty property : this.properties.values()) {
            property.setChanged(false);
            property.updateOriginal();
        }
        super.clearStatus();
    }

    /**
     * 抛弃变更
     *
     * @throws Exception 异常
     */
    public void discard() throws Exception {
        for (MariadbRecordProperty property : this.properties.values()) {
            property.discard();
        }
        super.clearStatus();
    }

    /**
     * 复制记录数据
     *
     * @param record 记录
     */
    public void copy(MariadbRecord record) {
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
    public DBRecordData getRecordData() {
        DBRecordData recordData = new DBRecordData();
        for (String column : this.columns()) {
            MariadbRecordProperty property = this.getProperty(column);
            if (property == null) {
                continue;
            }
            Object value = property.get();
            if (value == null) {
                continue;
            }
            // TODO: 时间戳处理
            MariadbColumn col = this.columns == null ? null : this.columns.column(column);
            if (col != null && col.supportTimestamp() && StringUtil.equalsIgnoreCase(value.toString(), "CURRENT_TIMESTAMP")) {
                continue;
            }
            recordData.put(property.getColumn(), value);
        }
        return recordData;
    }

    /**
     * 获取变更后记录数据
     *
     * @return 结果
     */
    public DBRecordData getChangedRecordData() {
        DBRecordData recordData = new DBRecordData();
        for (String column : this.columns()) {
            MariadbRecordProperty property = this.getProperty(column);
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
    public DBRecordData getOriginalRecordData() {
        DBRecordData recordData = new DBRecordData();
        for (String column : this.columns()) {
            MariadbRecordProperty property = this.getProperty(column);
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
        MariadbRecordProperty property = this.getProperty(column);
        return property != null && property.isChanged();
    }

    /**
     * 转换为map集合
     *
     * @return map集合
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, MariadbRecordProperty> value : this.properties.entrySet()) {
            map.put(value.getKey(), value.getValue().get());
        }
        return map;
    }

    /**
     * 是否可编辑
     *
     * @return 结果
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * 设置是否可编辑
     *
     * @param editable 是否可编辑
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public void destroy() {
        if (this.properties != null) {
            this.columns.clear();
            this.columns = null;
            for (MariadbRecordProperty property : this.properties.values()) {
                property.destroy();
            }
            this.properties.clear();
            this.properties = null;
        }
    }
}
