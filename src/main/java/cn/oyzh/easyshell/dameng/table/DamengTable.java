package cn.oyzh.easyshell.dameng.table;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBTable;
import javafx.beans.property.SimpleStringProperty;

/**
 * 达梦表
 *
 * @author oyzh
 * @since 2024/01/16
 */
public class DamengTable extends DBObject implements DBTable, ObjectCopier<DamengTable>, ObjectComparator<DamengTable> {

    /**
     * 是否有主键
     */
    private boolean hasPrimaryKey;

    /**
     * 表创建定义
     */
    private String createDefinition;

    /**
     * 表空间
     */
    private String tableSpace;

    /**
     * 设置表空间
     *
     * @param tableSpace 表空间
     */
    public void setTableSpace(String tableSpace) {
        this.tableSpace = tableSpace;
        super.putOriginalData("tableSpace", tableSpace);
    }

    /**
     * 表空间是否变更
     *
     * @return 结果
     */
    public boolean isTableSpaceChanged() {
        return super.checkOriginalData("tableSpace", this.tableSpace);
    }

    /**
     * 是否包含表空间
     *
     * @return 结果
     */
    public boolean hasTableSpace() {
        return this.getTableSpace() != null;
    }

    @Override
    public void copy(DamengTable table) {
        if (table != null) {
            this.setComment(table.getComment());
            this.setTableSpace(table.getTableSpace());
            this.setHasPrimaryKey(table.isHasPrimaryKey());
            this.setCreateDefinition(table.getCreateDefinition());
        }
    }

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 表名称
     */
    private SimpleStringProperty nameProperty;

    /**
     * 表注释
     */
    private SimpleStringProperty commentProperty;

    /**
     * 获取表名称属性
     *
     * @return 表名称属性
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
     * 获取表注释属性
     *
     * @return 表注释属性
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
     * 是否包含注释
     *
     * @return 结果
     */
    public boolean hasComment() {
        return this.getComment() != null;
    }

    @Override
    public boolean compare(DamengTable table) {
        if (table == null) {
            return false;
        }
        if (table == this) {
            return true;
        }
        if (!StringUtil.equals(this.getName(), table.getName())) {
            return false;
        }
        return StringUtil.equals(this.getSchema(), table.getSchema());
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
     * 是否有主键
     *
     * @return 结果
     */
    public boolean isHasPrimaryKey() {
        return hasPrimaryKey;
    }

    /**
     * 设置是否有主键
     *
     * @param hasPrimaryKey 是否有主键
     */
    public void setHasPrimaryKey(boolean hasPrimaryKey) {
        this.hasPrimaryKey = hasPrimaryKey;
    }

    /**
     * 获取表创建定义
     *
     * @return 表创建定义
     */
    public String getCreateDefinition() {
        return createDefinition;
    }

    /**
     * 设置表创建定义
     *
     * @param createDefinition 表创建定义
     */
    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }

    /**
     * 获取表空间
     *
     * @return 表空间
     */
    public String getTableSpace() {
        return tableSpace;
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

}
