package cn.oyzh.easyshell.mysql.view;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBView;
import javafx.beans.property.SimpleStringProperty;

import java.util.Collections;
import java.util.List;

/**
 * MySQL视图
 *
 * @author oyzh
 * @since 2024/06/28
 */
public class MysqlView extends DBObject implements DBView, ObjectCopier<MysqlView>, ObjectComparator<MysqlView> {

    /**
     * 定义者
     */
    private String definer;

    /**
     * 算法
     */
    private String algorithm;

    /**
     * 是否可变更
     */
    private boolean updatable;

    /**
     * 检查选项
     */
    private String checkOption;

    /**
     * 安全性
     */
    private String securityType;

    /**
     * 视图定义
     */
    private SimpleStringProperty definitionProperty;

    /**
     * 获取视图定义属性
     *
     * @return 视图定义属性
     */
    public SimpleStringProperty definitionProperty() {
        if (this.definitionProperty == null) {
            this.definitionProperty = new SimpleStringProperty();
        }
        return this.definitionProperty;
    }

    /**
     * 设置视图定义
     *
     * @param definition 视图定义
     */
    public void setDefinition(String definition) {
        this.definitionProperty().setValue(definition);
    }

    /**
     * 获取视图定义
     *
     * @return 视图定义
     */
    public String getDefinition() {
        return this.definitionProperty == null ? null : this.definitionProperty.get();
    }

    /**
     * 视图创建定义
     */
    private String createDefinition;

    /**
     * 设置视图创建定义
     *
     * @param createDefinition 视图创建定义
     */
    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }

    /**
     * 获取视图创建定义
     *
     * @return 视图创建定义
     */
    public String getCreateDefinition() {
        return this.createDefinition;
    }

    @Override
    public void copy(MysqlView f) {
        if (f != null) {
            this.setComment(f.getComment());
            this.setColumns(f.getColumns());
            this.setDefiner(f.getDefiner());
            this.setAlgorithm(f.getAlgorithm());
            this.setDefinition(f.getDefinition());
            this.setCheckOption(f.getCheckOption());
            this.setSecurityType(f.getSecurityType());
        }
    }

    /**
     * 是否包含检查选项
     *
     * @return 结果
     */
    public boolean hasCheckOption() {
        return StringUtil.isNotBlank(this.checkOption) && !StringUtil.equalsIgnoreCase(this.checkOption, "NONE");
    }

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 表字段
     */
    protected MysqlColumns columns;

    /**
     * 表名称
     */
    private SimpleStringProperty nameProperty;

    /**
     * 表注释
     */
    private SimpleStringProperty commentProperty;

    /**
     * 获取视图名称属性
     *
     * @return 视图名称属性
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
     * 获取视图注释属性
     *
     * @return 视图注释属性
     */
    public SimpleStringProperty commentProperty() {
        if (this.commentProperty == null) {
            this.commentProperty = new SimpleStringProperty();
        }
        return this.commentProperty;
    }

    @Override
    public void setComment(String comment) {
        this.commentProperty().setValue(comment);
    }

    @Override
    public String getComment() {
        return this.commentProperty == null ? null : this.commentProperty.get();
    }

    /**
     * 主键是否变更
     *
     * @return 结果
     */
    public boolean primaryKeyChanged() {
        if (this.hasColumns()) {
            boolean b1 = this.columns.primaryKeyChanged();
            if (b1) {
                return true;
            }
            for (MysqlColumn column : this.columns.createdList()) {
                if (column.isPrimaryKey()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取主键字段列表
     *
     * @return 主键字段列表
     */
    public List<MysqlColumn> primaryKeys() {
        if (this.hasColumns()) {
            return this.columns.primaryKeys();
        }
        return Collections.emptyList();
    }

    /**
     * 是否有主键
     *
     * @return 结果
     */
    public boolean hasPrimaryKey() {
        return CollectionUtil.isNotEmpty(this.primaryKeys());
    }

    /**
     * 是否包含字段
     *
     * @return 结果
     */
    public boolean hasColumns() {
        return this.columns != null && !this.columns.isEmpty();
    }

    /**
     * 获取字段集合
     *
     * @return 字段集合
     */
    public MysqlColumns columns() {
        if (this.columns == null) {
            this.columns = new MysqlColumns();
        }
        return this.columns;
    }

    @Override
    public boolean compare(MysqlView view) {
        if (view == null) {
            return false;
        }
        if (view == this) {
            return true;
        }
        if (!StringUtil.equals(this.getName(), view.getName())) {
            return false;
        }
        return StringUtil.equals(this.getDbName(), view.getDbName());
    }

    /**
     * 移除字段
     *
     * @param column 字段
     */
    public void removeColumn(MysqlColumn column) {
        if (column != null && this.columns != null) {
            this.columns().remove(column);
        }
    }

//    /**
//     * 是否新数据
//     *
//     * @return 结果
//     */
//
//    public boolean isNew() {
//        return StringUtil.isBlank(this.getName());
//    }

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
     * 获取算法
     *
     * @return 算法
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * 设置算法
     *
     * @param algorithm 算法
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    @Override
    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    /**
     * 获取检查选项
     *
     * @return 检查选项
     */
    public String getCheckOption() {
        return checkOption;
    }

    /**
     * 设置检查选项
     *
     * @param checkOption 检查选项
     */
    public void setCheckOption(String checkOption) {
        this.checkOption = checkOption;
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
     * 获取视图定义
     *
     * @return 视图定义
     */
    public String getDefinitionProperty() {
        return definitionProperty.get();
    }

    /**
     * 获取视图定义属性
     *
     * @return 视图定义属性
     */
    public SimpleStringProperty definitionPropertyProperty() {
        return definitionProperty;
    }

    /**
     * 设置视图定义
     *
     * @param definitionProperty 视图定义
     */
    public void setDefinitionProperty(String definitionProperty) {
        this.definitionProperty.set(definitionProperty);
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
     * 获取字段集合
     *
     * @return 字段集合
     */
    public MysqlColumns getColumns() {
        return columns;
    }

    /**
     * 设置字段集合
     *
     * @param columns 字段集合
     */
    public void setColumns(MysqlColumns columns) {
        this.columns = columns;
    }

    @Override
    public void destroy() {
        if (this.commentProperty != null) {
            this.commentProperty.unbind();
            this.commentProperty = null;
        }
        if (this.definitionProperty != null) {
            this.definitionProperty.unbind();
            this.definitionProperty = null;
        }
        super.destroy();
    }
}
