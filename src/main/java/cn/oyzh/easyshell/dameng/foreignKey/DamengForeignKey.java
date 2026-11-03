package cn.oyzh.easyshell.dameng.foreignKey;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBForeignKey;
import cn.oyzh.fx.db.DBObject;
import javafx.beans.property.SimpleStringProperty;

import java.util.Set;

/**
 * 达梦外键
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class DamengForeignKey extends DBObject implements DBForeignKey, ObjectCopier<DamengForeignKey> {

    /**
     * 外键名称
     */
    private String name;

    /**
     * 外键字段列表
     */
    private Set<String> columns;

    /**
     * 引用库名称
     */
    private SimpleStringProperty referencedDatabaseProperty;

    /**
     * 引用表名称
     */
    private SimpleStringProperty referencedTableProperty;

    /**
     * 引用字段列表
     */
    private Set<String> referencedColumns;

    /**
     * 外键删除策略
     */
    private String deletePolicy;

    /**
     * 外键更新策略
     */
    private String updatePolicy;

    /**
     * 获取原始名称
     *
     * @return 原始名称
     */
    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    /**
     * 获取引用库名称属性
     *
     * @return 引用库名称属性
     */
    public SimpleStringProperty referencedDatabaseProperty() {
        if (this.referencedDatabaseProperty == null) {
            this.referencedDatabaseProperty = new SimpleStringProperty();
        }
        return this.referencedDatabaseProperty;
    }

    /**
     * 获取引用表名称属性
     *
     * @return 引用表名称属性
     */
    public SimpleStringProperty referencedTableProperty() {
        if (this.referencedTableProperty == null) {
            this.referencedTableProperty = new SimpleStringProperty();
        }
        return this.referencedTableProperty;
    }

    /**
     * 设置删除策略
     *
     * @param deletePolicy 删除策略
     */
    public void setDeletePolicy(String deletePolicy) {
        this.deletePolicy = deletePolicy;
        super.putOriginalData("deletePolicy", deletePolicy);
    }

    /**
     * 设置更新策略
     *
     * @param updatePolicy 更新策略
     */
    public void setUpdatePolicy(String updatePolicy) {
        this.updatePolicy = updatePolicy;
        super.putOriginalData("updatePolicy", updatePolicy);
    }

    @Override
    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    @Override
    public void setColumns(Set<String> columns) {
        this.columns = columns;
        super.putOriginalData("columns", columns);
    }

    @Override
    public void setReferencedDatabase(String referencedDatabase) {
        this.referencedDatabaseProperty().set(referencedDatabase);
        super.putOriginalData("referencedDatabase", referencedDatabase);
    }

    @Override
    public String getReferencedDatabase() {
        String dbName = null;
        if (this.referencedDatabaseProperty != null) {
            dbName = this.referencedDatabaseProperty.get();
        }
        return dbName;
    }

    @Override
    public void setReferencedTable(String referencedTable) {
        this.referencedTableProperty().set(referencedTable);
        super.putOriginalData("referencedTable", referencedTable);
    }

    @Override
    public String getReferencedTable() {
        if (this.referencedTableProperty == null) {
            return null;
        }
        return this.referencedTableProperty.get();
    }

    @Override
    public void setReferencedColumns(Set<String> referencedColumns) {
        this.referencedColumns = referencedColumns;
        super.putOriginalData("referencedColumns", referencedColumns);
    }

    @Override
    public void copy(DamengForeignKey t1) {
        if (t1 != null) {
            this.name = t1.name;
            this.columns = t1.columns;
            this.deletePolicy = t1.deletePolicy;
            this.updatePolicy = t1.updatePolicy;
            this.referencedColumns = t1.referencedColumns;
            this.setReferencedTable(t1.getReferencedTable());
            this.setReferencedDatabase(t1.getReferencedDatabase());
        }
    }

    @Override
    public boolean isInvalid() {
        return DBForeignKey.super.isInvalid() || CollectionUtil.isEmpty(this.referencedColumns) || CollectionUtil.isEmpty(this.columns)
                || StringUtil.isBlank(this.getReferencedTable()) || StringUtil.isBlank(this.getReferencedDatabase());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getColumns() {
        return columns;
    }

    /**
     * 获取删除策略
     *
     * @return 删除策略
     */
    public String getDeletePolicy() {
        return deletePolicy;
    }

    /**
     * 获取更新策略
     *
     * @return 更新策略
     */
    public String getUpdatePolicy() {
        return updatePolicy;
    }

    @Override
    public Set<String> getReferencedColumns() {
        return referencedColumns;
    }
}
