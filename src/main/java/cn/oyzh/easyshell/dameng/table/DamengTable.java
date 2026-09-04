package cn.oyzh.easyshell.dameng.table;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBTable;
import javafx.beans.property.SimpleStringProperty;

/**
 * db表
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

    public void setTableSpace(String tableSpace) {
        this.tableSpace = tableSpace;
        super.putOriginalData("tableSpace", tableSpace);
    }

    public boolean isTableSpaceChanged() {
        return super.checkOriginalData("tableSpace", this.tableSpace);
    }

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

    /**
     * 是否新数据
     *
     * @return 结果
     */

    public boolean isNew() {
        return StringUtil.isBlank(this.getName());
    }

    public boolean isHasPrimaryKey() {
        return hasPrimaryKey;
    }

    public void setHasPrimaryKey(boolean hasPrimaryKey) {
        this.hasPrimaryKey = hasPrimaryKey;
    }

    public String getCreateDefinition() {
        return createDefinition;
    }

    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }

    public String getTableSpace() {
        return tableSpace;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

}
