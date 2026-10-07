package cn.oyzh.easyshell.dameng.routine;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBRoutineSchema;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

/**
 * 达梦存储程序结构
 *
 * @author oyzh
 * @since 2024/06/28
 */
public class DamengRoutineSchema implements DBRoutineSchema, ObjectComparator<DamengRoutineSchema> {

    /**
     * 参数列表
     */
    private List<DamengRoutineParam> params;

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 安全性
     */
    private String securityType;

    /**
     * 特征
     */
    private String characteristic;

    /**
     * 程序名称
     */
    private SimpleStringProperty nameProperty;

    /**
     * 程序定义
     */
    private SimpleStringProperty definitionProperty;

    /**
     * 获取程序名称属性
     *
     * @return 程序名称属性
     */
    public SimpleStringProperty nameProperty() {
        if (this.nameProperty == null) {
            this.nameProperty = new SimpleStringProperty();
        }
        return this.nameProperty;
    }

    @Override
    public void setName(String name) {
        this.nameProperty().setValue(name);
    }

    @Override
    public String getName() {
        return this.nameProperty == null ? null : this.nameProperty.get();
    }

    /**
     * 获取程序定义属性
     *
     * @return 程序定义属性
     */
    public SimpleStringProperty definitionProperty() {
        if (this.definitionProperty == null) {
            this.definitionProperty = new SimpleStringProperty();
        }
        return this.definitionProperty;
    }

    /**
     * 设置程序定义
     *
     * @param definition 程序定义
     */
    public void setDefinition(String definition) {
        this.definitionProperty().setValue(definition);
    }

    /**
     * 获取程序定义
     *
     * @return 程序定义
     */
    public String getDefinition() {
        return this.definitionProperty == null ? null : this.definitionProperty.get();
    }

    /**
     * 程序创建定义
     */
    private SimpleStringProperty createDefinitionProperty;

    /**
     * 获取程序创建定义属性
     *
     * @return 程序创建定义属性
     */
    public SimpleStringProperty createDefinitionProperty() {
        if (this.createDefinitionProperty == null) {
            this.createDefinitionProperty = new SimpleStringProperty();
        }
        return this.createDefinitionProperty;
    }

    /**
     * 设置程序创建定义
     *
     * @param createDefinition 程序创建定义
     */
    public void setCreateDefinition(String createDefinition) {
        this.createDefinitionProperty().setValue(createDefinition);
    }

    /**
     * 获取程序创建定义
     *
     * @return 程序创建定义
     */
    public String getCreateDefinition() {
        return this.createDefinitionProperty == null ? null : this.createDefinitionProperty.get();
    }

    @Override
    public boolean compare(DamengRoutineSchema routine) {
        if (routine == null) {
            return false;
        }
        if (routine == this) {
            return true;
        }
        if (!StringUtil.equals(this.getSchema(), routine.getSchema())) {
            return false;
        }
        return StringUtil.equals(this.getName(), routine.getName());
    }

    /**
     * 获取参数列表
     *
     * @return 参数列表
     */
    public List<DamengRoutineParam> getParams() {
        return params;
    }

    /**
     * 设置参数列表
     *
     * @param params 参数列表
     */
    public void setParams(List<DamengRoutineParam> params) {
        this.params = params;
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 设置模式名称
     *
     * @param schema 模式名称
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 获取安全性
     *
     * @return 安全性
     */
    public String getSecurityType() {
        return securityType;
    }

    /**
     * 设置安全性
     *
     * @param securityType 安全性
     */
    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    /**
     * 获取特征
     *
     * @return 特征
     */
    public String getCharacteristic() {
        return characteristic;
    }

    /**
     * 设置特征
     *
     * @param characteristic 特征
     */
    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }
}
