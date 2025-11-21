package cn.oyzh.easyshell.mysql.view;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBObjectStatus;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import javafx.beans.property.SimpleStringProperty;

import java.util.Collections;
import java.util.List;

/**
 * db视图
 *
 * @author oyzh
 * @since 2024/06/28
 */
public class MysqlView extends DBObjectStatus implements ObjectCopier<MysqlView>, ObjectComparator<MysqlView> {

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
     * 视图创建定义
     */
    private String createDefinition;

    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }

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

    public SimpleStringProperty commentProperty() {
        if (this.commentProperty == null) {
            this.commentProperty = new SimpleStringProperty();
        }
        return this.commentProperty;
    }

    public void setComment(String comment) {
        this.commentProperty().setValue(comment);
    }

    public String getComment() {
        return this.commentProperty == null ? null : this.commentProperty.get();
    }

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

    public List<MysqlColumn> primaryKeys() {
        if (this.hasColumns()) {
            return this.columns.primaryKeys();
        }
        return Collections.emptyList();
    }

    public boolean hasPrimaryKey() {
        return CollectionUtil.isNotEmpty(this.primaryKeys());
    }

    public boolean hasColumns() {
        return this.columns != null && !this.columns.isEmpty();
    }

    public boolean hasComment() {
        return this.getComment() != null;
    }

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

    public void removeColumn(MysqlColumn column) {
        if (column != null && this.columns != null) {
            this.columns().remove(column);
        }
    }

    /**
     * 是否新数据
     *
     * @return 结果
     */

    public boolean isNew() {
        return StringUtil.isBlank(this.getName());
    }

    public String getDefiner() {
        return definer;
    }

    public void setDefiner(String definer) {
        this.definer = definer;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    public String getCheckOption() {
        return checkOption;
    }

    public void setCheckOption(String checkOption) {
        this.checkOption = checkOption;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public String getDefinitionProperty() {
        return definitionProperty.get();
    }

    public SimpleStringProperty definitionPropertyProperty() {
        return definitionProperty;
    }

    public void setDefinitionProperty(String definitionProperty) {
        this.definitionProperty.set(definitionProperty);
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public MysqlColumns getColumns() {
        return columns;
    }

    public void setColumns(MysqlColumns columns) {
        this.columns = columns;
    }
}
