package cn.oyzh.easyshell.dameng.foreignKey;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBForeignKey;
import cn.oyzh.fx.db.DBObject;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 达梦外键
 *
 * @author oyzh
 * @since 2024/01/25
 */
public class DamengForeignKey extends DBObject implements DBForeignKey,ObjectCopier<DamengForeignKey> {

    /**
     * 外键名称
     */
    private String name;

    /**
     * 外键字段列表
     */
    private List<String> columns;

    /**
     * 引用库名称
     */
    private SimpleStringProperty primaryKeyDatabaseProperty;

    /**
     * 引用表名称
     */
    private SimpleStringProperty primaryKeyTableProperty;

    /**
     * 引用字段列表
     */
    private List<String> primaryKeyColumns;

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
    public SimpleStringProperty primaryKeyDatabaseProperty() {
        if (this.primaryKeyDatabaseProperty == null) {
            this.primaryKeyDatabaseProperty = new SimpleStringProperty();
        }
        return this.primaryKeyDatabaseProperty;
    }

    /**
     * 获取引用表名称属性
     *
     * @return 引用表名称属性
     */
    public SimpleStringProperty primaryKeyTableProperty() {
        if (this.primaryKeyTableProperty == null) {
            this.primaryKeyTableProperty = new SimpleStringProperty();
        }
        return this.primaryKeyTableProperty;
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

    /**
     * 设置外键字段列表
     *
     * @param columns 外键字段列表
     */
    public void setColumns(List<String> columns) {
        this.columns = columns;
        super.putOriginalData("columns", columns);
    }

    /**
     * 设置引用库名称
     *
     * @param primaryKeyDatabase 引用库名称
     */
    public void setPrimaryKeyDatabase(String primaryKeyDatabase) {
        this.primaryKeyDatabaseProperty().set(primaryKeyDatabase);
        super.putOriginalData("primaryKeyDatabase", primaryKeyDatabase);
    }

    /**
     * 获取引用库名称
     *
     * @return 引用库名称
     */
    public String getPrimaryKeyDatabase() {
        String dbName = null;
        if (this.primaryKeyDatabaseProperty != null) {
            dbName = this.primaryKeyDatabaseProperty.get();
        }
        return dbName;
    }

    /**
     * 设置引用表名称
     *
     * @param primaryKeyTable 引用表名称
     */
    public void setPrimaryKeyTable(String primaryKeyTable) {
        this.primaryKeyTableProperty().set(primaryKeyTable);
        super.putOriginalData("primaryKeyTable", primaryKeyTable);
    }

    /**
     * 获取引用表名称
     *
     * @return 引用表名称
     */
    public String getPrimaryKeyTable() {
        if (this.primaryKeyTableProperty == null) {
            return null;
        }
        return this.primaryKeyTableProperty.get();
    }

    /**
     * 设置引用字段列表
     *
     * @param primaryKeyColumns 引用字段列表
     */
    public void setPrimaryKeyColumns(List<String> primaryKeyColumns) {
        this.primaryKeyColumns = primaryKeyColumns;
        super.putOriginalData("primaryKeyColumns", primaryKeyColumns);
    }

    /**
     * 添加外键字段
     *
     * @param columnName 字段名称
     */
    public void addColumn(String columnName) {
        if (this.columns == null) {
            this.setColumns(new ArrayList<>());
        }
        this.columns.add(columnName);
    }

    /**
     * 添加引用字段
     *
     * @param columnName 字段名称
     */
    public void addPrimaryKeyColumn(String columnName) {
        if (this.primaryKeyColumns == null) {
            this.setPrimaryKeyColumns(new ArrayList<>());
        }
        this.primaryKeyColumns.add(columnName);
    }

    @Override
    public void copy(DamengForeignKey t1) {
        if (t1 != null) {
            this.name = t1.name;
            this.columns = t1.columns;
            this.deletePolicy = t1.deletePolicy;
            this.updatePolicy = t1.updatePolicy;
            this.primaryKeyColumns = t1.primaryKeyColumns;
            this.setPrimaryKeyTable(t1.getPrimaryKeyTable());
            this.setPrimaryKeyDatabase(t1.getPrimaryKeyDatabase());
        }
    }

    @Override
    public boolean isInvalid() {
        return DBForeignKey.super.isInvalid() || CollectionUtil.isEmpty(this.primaryKeyColumns) || CollectionUtil.isEmpty(this.columns)
                || StringUtil.isBlank(this.getPrimaryKeyTable()) || StringUtil.isBlank(this.getPrimaryKeyDatabase());
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取外键字段列表
     *
     * @return 外键字段列表
     */
    public List<String> getColumns() {
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

    /**
     * 获取引用字段列表
     *
     * @return 引用字段列表
     */
    public List<String> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }
}
