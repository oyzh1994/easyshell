package cn.oyzh.easyshell.dto.redis;

import javafx.beans.property.SimpleStringProperty;

/**
 * redis信息属性项目
 *
 * @author oyzh
 * @since 2023/08/01
 */
public class RedisInfoPropItem {

    /**
     * 名称
     */
    private SimpleStringProperty nameProperty;

    /**
     * 值
     */
    private SimpleStringProperty valueProperty;

    public RedisInfoPropItem(String name, String value) {
        this.setName(name);
        this.setValue(value);
    }

    public SimpleStringProperty nameProperty() {
        if (this.nameProperty == null) {
            this.nameProperty = new SimpleStringProperty();
        }
        return nameProperty;
    }

    public SimpleStringProperty valueProperty() {
        if (this.valueProperty == null) {
            this.valueProperty = new SimpleStringProperty();
        }
        return valueProperty;
    }

    public void setName(String value) {
        this.nameProperty().setValue(value);
    }

    public String getName() {
        return this.nameProperty == null ? null : this.nameProperty.get();
    }

    public void setValue(String value) {
        this.valueProperty().setValue(value);
    }

    public String getValue() {
        return this.valueProperty == null ? null : this.valueProperty.get();
    }
}
