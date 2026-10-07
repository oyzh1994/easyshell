package cn.oyzh.easyshell.dto.redis;

import javafx.beans.property.SimpleStringProperty;

/**
 * redis信息属性项目
 *
 * @author oyzh
 * @since 2023/08/01
 */
public class ShellRedisInfoPropItem {

    /**
     * 名称
     */
    private SimpleStringProperty nameProperty;

    /**
     * 值
     */
    private SimpleStringProperty valueProperty;

    /**
     * 构造函数
     *
     * @param name  名称
     * @param value 值
     */
    public ShellRedisInfoPropItem(String name, String value) {
        this.setName(name);
        this.setValue(value);
    }

    /** 获取名称属性 */
    public SimpleStringProperty nameProperty() {
        if (this.nameProperty == null) {
            this.nameProperty = new SimpleStringProperty();
        }
        return nameProperty;
    }

    /** 获取值属性 */
    public SimpleStringProperty valueProperty() {
        if (this.valueProperty == null) {
            this.valueProperty = new SimpleStringProperty();
        }
        return valueProperty;
    }

    /** 设置名称 */
    public void setName(String value) {
        this.nameProperty().setValue(value);
    }

    /** 获取名称 */
    public String getName() {
        return this.nameProperty == null ? null : this.nameProperty.get();
    }

    /** 设置值 */
    public void setValue(String value) {
        this.valueProperty().setValue(value);
    }

    /** 获取值 */
    public String getValue() {
        return this.valueProperty == null ? null : this.valueProperty.get();
    }
}
