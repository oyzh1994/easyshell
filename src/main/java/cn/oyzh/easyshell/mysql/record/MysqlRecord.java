package cn.oyzh.easyshell.mysql.record;


import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBRecord;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.DBRecordProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL记录
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlRecord extends DBObject implements DBRecord, ObjectCopier<MysqlRecord> {

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

    /**
     * 构造记录
     *
     * @param columns 字段列表
     */
    public MysqlRecord(MysqlColumns columns) {
        this(columns, false);
    }

    /**
     * 构造记录
     *
     * @param columns 字段列表
     */
    public MysqlRecord(List<MysqlColumn> columns) {
        this(new MysqlColumns(columns), false);
    }

    /**
     * 构造记录
     *
     * @param columns  字段列表
     * @param readonly 是否只读
     */
    public MysqlRecord(List<MysqlColumn> columns, boolean readonly) {
        this(new MysqlColumns(columns), readonly);
    }

    /**
     * 构造记录
     *
     * @param columns  字段列表
     * @param readonly 是否只读
     */
    public MysqlRecord(MysqlColumns columns, boolean readonly) {
        this.columns = columns;
        this.readonly = readonly;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public MysqlColumns getColumns() {
        return columns;
    }

    /**
     * 记录属性集合
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
        MysqlRecordProperty property = (MysqlRecordProperty) this.getProperty(column);
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
        MysqlRecordProperty property = (MysqlRecordProperty) this.getProperty(column.getName());
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

    @Override
    public void discard() throws Exception {
        for (MysqlRecordProperty property : this.properties.values()) {
            property.discard();
        }
        super.clearStatus();
    }

    @Override
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

    @Override
    public DBRecordData getRecordData() {
        DBRecordData recordData = new DBRecordData();
        for (MysqlColumn column : this.getColumns()) {
            MysqlRecordProperty property = (MysqlRecordProperty) this.getProperty(column.getName());
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
            if (value != null && column.supportTimestamp() && StringUtil.equalsIgnoreCase(value.toString(), "CURRENT_TIMESTAMP")) {
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
        for (Map.Entry<String, MysqlRecordProperty> value : this.properties.entrySet()) {
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
            for (MysqlRecordProperty property : this.properties.values()) {
                property.destroy();
            }
            this.properties.clear();
            this.properties = null;
        }
    }
}
