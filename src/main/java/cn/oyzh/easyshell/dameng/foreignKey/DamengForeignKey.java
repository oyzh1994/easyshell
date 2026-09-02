package cn.oyzh.easyshell.dameng.foreignKey;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBForeignKey;
import cn.oyzh.fx.db.DBObjectStatus;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * db表外键
 *
 * @author oyzh
 * @since 2024/01/25
 */
public class DamengForeignKey extends DBObjectStatus implements DBForeignKey,ObjectCopier<DamengForeignKey> {

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

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    public SimpleStringProperty primaryKeyDatabaseProperty() {
        if (this.primaryKeyDatabaseProperty == null) {
            this.primaryKeyDatabaseProperty = new SimpleStringProperty();
        }
        return this.primaryKeyDatabaseProperty;
    }

    public SimpleStringProperty primaryKeyTableProperty() {
        if (this.primaryKeyTableProperty == null) {
            this.primaryKeyTableProperty = new SimpleStringProperty();
        }
        return this.primaryKeyTableProperty;
    }

    public void setDeletePolicy(String deletePolicy) {
        this.deletePolicy = deletePolicy;
        super.putOriginalData("deletePolicy", deletePolicy);
    }

    public void setUpdatePolicy(String updatePolicy) {
        this.updatePolicy = updatePolicy;
        super.putOriginalData("updatePolicy", updatePolicy);
    }

    @Override
    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
        super.putOriginalData("columns", columns);
    }

    public void setPrimaryKeyDatabase(String primaryKeyDatabase) {
        this.primaryKeyDatabaseProperty().set(primaryKeyDatabase);
        super.putOriginalData("primaryKeyDatabase", primaryKeyDatabase);
    }

    public String getPrimaryKeyDatabase() {
        String dbName = null;
        if (this.primaryKeyDatabaseProperty != null) {
            dbName = this.primaryKeyDatabaseProperty.get();
        }
        return dbName;
    }

    public void setPrimaryKeyTable(String primaryKeyTable) {
        this.primaryKeyTableProperty().set(primaryKeyTable);
        super.putOriginalData("primaryKeyTable", primaryKeyTable);
    }

    public String getPrimaryKeyTable() {
        if (this.primaryKeyTableProperty == null) {
            return null;
        }
        return this.primaryKeyTableProperty.get();
    }

    public void setPrimaryKeyColumns(List<String> primaryKeyColumns) {
        this.primaryKeyColumns = primaryKeyColumns;
        super.putOriginalData("primaryKeyColumns", primaryKeyColumns);
    }

    public void addColumn(String columnName) {
        if (this.columns == null) {
            this.setColumns(new ArrayList<>());
        }
        this.columns.add(columnName);
    }

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

    public List<String> getColumns() {
        return columns;
    }

    public String getDeletePolicy() {
        return deletePolicy;
    }

    public String getUpdatePolicy() {
        return updatePolicy;
    }

    public List<String> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }
}
