package cn.oyzh.easyshell.mysql.routine;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBRoutineSchema;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

/**
 * MySQL存储程序结构
 *
 * @author oyzh
 * @since 2024/06/28
 */
public class MysqlRoutineSchema implements DBRoutineSchema, ObjectComparator<MysqlRoutineSchema> {

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

    /**
     * 获取程序创建定义
     *
     * @return 程序创建定义
     */
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

    /**
     * 获取参数列表
     *
     * @return 参数列表
     */
    public List<MysqlRoutineParam> getParams() {
        return params;
    }

    /**
     * 设置参数列表
     *
     * @param params 参数列表
     */
    public void setParams(List<MysqlRoutineParam> params) {
        this.params = params;
    }

    /**
     * 获取库名称
     *
     * @return 库名称
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * 设置库名称
     *
     * @param dbName 库名称
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * 获取注释
     *
     * @return 注释
     */
    public String getComment() {
        return comment;
    }

    /**
     * 设置注释
     *
     * @param comment 注释
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 获取定义者
     *
     * @return 定义者
     */
    public String getDefiner() {
        return definer;
    }

    /**
     * 设置定义者
     *
     * @param definer 定义者
     */
    public void setDefiner(String definer) {
        this.definer = definer;
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
