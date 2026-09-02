package cn.oyzh.easyshell.dameng.record;

import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.util.dameng.DamengDataUtil;
import cn.oyzh.easyshell.util.dameng.DamengNodeUtil;
import cn.oyzh.easyshell.util.dameng.DamengRecordUtil;
import cn.oyzh.fx.db.DBRecordProperty;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;

/**
 * db表记录属性
 *
 * @author oyzh
 * @since 2024/01/31
 */
public class DamengRecordProperty extends DBRecordProperty {

    /**
     * 表字段
     */
    private DamengColumn column;

    /**
     * 表记录
     */
    private DamengRecord record;

    public DamengRecordProperty(DamengRecord record, DamengColumn column, Object value, boolean readonly) {
        super(value);
        this.column = column;
        this.record = record;
        // this.columns = columns;
        if (!readonly) {
            this.original = value;
        }
        this.readonly = readonly;
    }

    @Override
    public Object get() {
        if (this.readonly || !this.isChanged() || this.node == null) {
            return super.get();
        }
        if (this.setToNullFlag) {
            return null;
        }
        try {
            return DamengNodeUtil.getNodeVal(this.node);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    @Override
    public void set(Object newValue) {
        super.set(newValue);
        if (this.node != null) {
            DamengNodeUtil.setNodeVal(node, newValue);
        }
    }

    @Override
    public Object getValue() {
        if (this.readonly) {
            return DamengRecordUtil.formatValue(super.getValue(), this.column);
        }
        if (this.node == null) {
            this.node = DamengRecordUtil.getNode(this, super.getValue(), this.column);
            TableViewUtil.rowOnCtrlS(this.node);
            TableViewUtil.selectRowOnMouseClicked(this.node);
        }
        return this.node;
    }

    @Override
    public void discard() {
        if (this.isChanged() && this.node != null) {
            DamengNodeUtil.setNodeVal(this.node, super.get());
        }
        super.setChanged(false);
    }

    @Override
    public void setChanged(boolean changed) {
        this.changedProperty().set(changed);
        DBStatusListener listener = DBStatusListenerManager.getListener(this.column.getSchema() + ":" + this.column.getTableName());
        if (listener != null) {
            listener.changed(null, null, null);
        }
        this.setToNullFlag = false;
    }

    public void updateOriginal() {
        try {
            if (this.node != null) {
                super.set(DamengNodeUtil.getNodeVal(this.node));
                this.original = super.get();
            }
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 复制为insert语句
     */
    public void vCopyAsInsertSql() {
        DamengColumns columns = this.record.getColumns();
        String sql = DamengDataUtil.toInsertSql(columns, this.record, true);
        ClipboardUtil.copy(sql);
    }

    /**
     * 复制为update语句
     */
    public void vCopyAsUpdateSql() {
        DamengColumns columns = this.record.getColumns();
        String sql = DamengDataUtil.toUpdateSql(columns, this.record);
        ClipboardUtil.copy(sql);
    }

    public DamengColumn getColumn() {
        return column;
    }

    public void setColumn(DamengColumn column) {
        this.column = column;
    }

    @Override
    public void destroy() {
        if (this.node != null) {
            this.column = null;
            this.record = null;
            super.destroy();
        }
    }
}
