package cn.oyzh.easyshell.mysql;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.BooleanUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author oyzh
 * @since 2024/07/13
 */
public class DBObjectStatus {

    /**
     * 对象是否变更
     */
    private SimpleBooleanProperty changedProperty;

    /**
     * 对象是否删除
     */
    private SimpleBooleanProperty deletedProperty;

    /**
     * 对象是否新增
     */
    private SimpleBooleanProperty createdProperty;

    /**
     * 变更数据
     */
    private Map<String, Boolean> changedFlag;

    /**
     * 原始数据
     */
    private Map<String, Object> originalData;

    public SimpleBooleanProperty changedProperty() {
        if (this.changedProperty == null) {
            this.changedProperty = new SimpleBooleanProperty();
        }
        return this.changedProperty;
    }

    public SimpleBooleanProperty deletedProperty() {
        if (this.deletedProperty == null) {
            this.deletedProperty = new SimpleBooleanProperty();
        }
        return this.deletedProperty;
    }

    public SimpleBooleanProperty createdProperty() {
        if (this.createdProperty == null) {
            this.createdProperty = new SimpleBooleanProperty();
        }
        return this.createdProperty;
    }

    private Map<String, Boolean> changedFlag() {
        if (this.changedFlag == null) {
            this.changedFlag = new HashMap<>();
        }
        return this.changedFlag;
    }

    private void setChangedFlag(String key, Boolean value) {
        // 已变更
        if (BooleanUtil.isTrue(value)) {
            this.changedFlag().put(key, value);
        } else {// 未变更
            this.changedFlag().remove(key);
        }
        this.setChanged(!this.changedFlag().isEmpty());
    }

    protected void clearChangedFlag() {
        if (this.changedFlag != null) {
            this.changedFlag().clear();
        }
    }

    protected Map<String, Object> originalData() {
        if (this.originalData == null) {
            this.originalData = new HashMap<>();
        }
        return this.originalData;
    }

    protected void putOriginalData(String key, Object value) {
        JulLog.info("putOriginalData: key={}, value={}", key, value);
        if (this.originalData().containsKey(key)) {
            Object val = this.getOriginalData(key);
            this.setChangedFlag(key, !Objects.equals(val, value));
        } else {
            this.originalData().put(key, value);
        }
    }

    protected Object getOriginalData(String key) {
        if (this.originalData == null) {
            return null;
        }
        return this.originalData().get(key);
    }

    public void clearOriginalData() {
        if (this.originalData != null) {
            this.originalData().clear();
        }
    }

    protected Boolean checkOriginalData(String key, Object currentData) {
        return !Objects.equals(this.getOriginalData(key), currentData);
    }

    public void initStatus() {

    }

    public void setChanged(boolean changed) {
        this.changedProperty().set(changed);
        this.updateStatus();
    }

    public boolean isChanged() {
        return this.changedProperty != null && this.changedProperty.get();
    }

    public void setDeleted(boolean deleted) {
        this.deletedProperty().set(deleted);
        this.updateStatus();
    }

    public boolean isDeleted() {
        return this.deletedProperty != null && this.deletedProperty.get();
    }

    public void setCreated(boolean created) {
        this.createdProperty().set(created);
        this.updateStatus();
    }

    public boolean isCreated() {
        return this.createdProperty != null && this.createdProperty.get();
    }

    public void clearStatus() {
        this.setChanged(false);
        this.setCreated(false);
        this.setDeleted(false);
        this.updateStatus();
        this.clearChangedFlag();
    }

    public void updateStatus() {
        if (this.isCreated()) {
            this.statusProperty().set("+");
        } else if (this.isChanged()) {
            this.statusProperty().set("*");
        } else {
            this.statusProperty().set("");
        }
    }

    private SimpleStringProperty statusProperty;

    public SimpleStringProperty statusProperty() {
        if (this.statusProperty == null) {
            this.statusProperty = new SimpleStringProperty();
        }
        return statusProperty;
    }

    // @Override
    // public boolean equals(Object o) {
    //     return o == this;
    // }

    public String getStatus() {
        return this.statusProperty().get();
    }
}
