package cn.oyzh.easyshell.mysql.routine;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.util.StringUtil;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

/**
 * db程序
 *
 * @author oyzh
 * @since 2024/06/28
 */
public class MysqlRoutineSchema implements ObjectComparator<MysqlRoutineSchema> {

    /**
     * 参数列表
     */
    private List<MysqlRoutineParam> params;

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 注释
     */
    private String comment;

    /**
     * 定义者
     */
    private String definer;

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

    public void setName(String name) {
        this.nameProperty().setValue(name);
    }

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
        if (StringUtil.isNotBlank(createDefinition)) {
            String[] arr = createDefinition.split(" ");
            for (String string : arr) {
                if (StringUtil.startWithIgnoreCase(string, "DEFINER=")) {
                    this.definer = string.substring(8);
                    break;
                }
            }
            String[] arr1 = createDefinition.split("COMMENT '");
            if (arr1.length >= 2) {
                this.comment = arr1[1].substring(0, arr1[1].indexOf("'"));
            }
            String[] arr2 = createDefinition.split("COMMENT \"");
            if (arr2.length >= 2) {
                this.comment = arr2[1].substring(0, arr2[1].indexOf("\""));
            }
        }
    }

    public String getCreateDefinition() {
        return this.createDefinitionProperty == null ? null : this.createDefinitionProperty.get();
    }

    @Override
    public boolean compare(MysqlRoutineSchema routine) {
        if (routine == null) {
            return false;
        }
        if (routine == this) {
            return true;
        }
        if (!StringUtil.equals(this.getDbName(), routine.getDbName())) {
            return false;
        }
        return StringUtil.equals(this.getName(), routine.getName());
    }

    public boolean isNew() {
        return StringUtil.isBlank(this.getName());
    }

    public List<MysqlRoutineParam> getParams() {
        return params;
    }

    public void setParams(List<MysqlRoutineParam> params) {
        this.params = params;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDefiner() {
        return definer;
    }

    public void setDefiner(String definer) {
        this.definer = definer;
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
