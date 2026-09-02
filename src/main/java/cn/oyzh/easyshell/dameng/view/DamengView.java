package cn.oyzh.easyshell.dameng.view;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.fx.db.DBObjectStatus;
import cn.oyzh.fx.db.DBView;
import javafx.beans.property.SimpleStringProperty;

import java.util.Collections;
import java.util.List;

/**
 * db视图
 *
 * @author oyzh
 * @since 2024/06/28
 */
public class DamengView extends DBObjectStatus implements DBView, ObjectCopier<DamengView>, ObjectComparator<DamengView> {

    /**
     * 是否可变更
     */
    private boolean updatable;

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
    public void copy(DamengView f) {
        if (f != null) {
            this.setComment(f.getComment());
            this.setColumns(f.getColumns());
            this.setUpdatable(f.isUpdatable());
            this.setDefinition(f.getDefinition());
            this.setSecurityType(f.getSecurityType());
        }
    }

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 表字段
     */
    protected DamengColumns columns;

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

    @Override
    public void setName(String name) {
        this.nameProperty().setValue(name);
    }

    @Override
    public String getName() {
        return this.nameProperty == null ? null : this.nameProperty.get();
    }

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

    public boolean primaryKeyChanged() {
        if (this.hasColumns()) {
            boolean b1 = this.columns.primaryKeyChanged();
            if (b1) {
                return true;
            }
            for (DamengColumn column : this.columns.createdList()) {
                if (column.isPrimaryKey()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<DamengColumn> primaryKeys() {
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

    public DamengColumns columns() {
        if (this.columns == null) {
            this.columns = new DamengColumns();
        }
        return this.columns;
    }

    @Override
    public boolean compare(DamengView view) {
        if (view == null) {
            return false;
        }
        if (view == this) {
            return true;
        }
        if (!StringUtil.equals(this.getName(), view.getName())) {
            return false;
        }
        return StringUtil.equals(this.getSchema(), view.getSchema());
    }

    public void removeColumn(DamengColumn column) {
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

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    @Override
    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
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

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public DamengColumns getColumns() {
        return columns;
    }

    public void setColumns(DamengColumns columns) {
        this.columns = columns;
    }
}
