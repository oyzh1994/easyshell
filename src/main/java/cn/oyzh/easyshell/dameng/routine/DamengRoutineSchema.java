package cn.oyzh.easyshell.dameng.routine;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBRoutineSchema;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

/**
 * db程序
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
     * 库名称
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

    public SimpleStringProperty definitionProperty() {
        if (this.definitionProperty == null) {
            this.definitionProperty = new SimpleStringProperty();
        }
        return this.definitionProperty;
    }

    public void setDefinition(String definition) {
        this.definitionProperty().setValue(definition);
    }

    public String getDefinition() {
        return this.definitionProperty == null ? null : this.definitionProperty.get();
    }

    /**
     * 程序创建定义
     */
    private SimpleStringProperty createDefinitionProperty;

    public SimpleStringProperty createDefinitionProperty() {
        if (this.createDefinitionProperty == null) {
            this.createDefinitionProperty = new SimpleStringProperty();
        }
        return this.createDefinitionProperty;
    }

    public void setCreateDefinition(String createDefinition) {
        this.createDefinitionProperty().setValue(createDefinition);
    }

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

    public List<DamengRoutineParam> getParams() {
        return params;
    }

    public void setParams(List<DamengRoutineParam> params) {
        this.params = params;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }
}
