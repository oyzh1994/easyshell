package cn.oyzh.easyshell.sqlite.record;

import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteDataUtil;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteNodeUtil;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteRecordUtil;
import cn.oyzh.fx.db.DBRecordProperty;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;

/**
 * SQLite记录属性
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteRecordProperty extends DBRecordProperty {

    /**
     * 表记录
     */
    private SQLiteRecord record;

    /**
     * 构造记录属性
     *
     * @param record   记录
     * @param column   字段
     * @param value    值
     * @param readonly 是否只读
     */
    public SQLiteRecordProperty(SQLiteRecord record, SQLiteColumn column, Object value, boolean readonly) {
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
            return ShellSqliteNodeUtil.getNodeVal(this.node);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    @Override
    public void set(Object newValue) {
        super.set(newValue);
        if (this.node != null) {
            ShellSqliteNodeUtil.setNodeVal(this.node, newValue);
        }
    }

    @Override
    public Object getValue() {
        if (this.readonly) {
            return ShellSqliteRecordUtil.formatValue(super.getValue(), this.getColumn());
        }
        if (this.node == null) {
            this.node = ShellSqliteRecordUtil.getNode(this, super.get(), this.getColumn());
            TableViewUtil.rowOnCtrlS(this.node);
            TableViewUtil.selectRowOnMouseClicked(this.node);
        }
        return this.node;
    }

    @Override
    public void discard() {
        if (this.isChanged() && this.node != null) {
            ShellSqliteNodeUtil.setNodeVal(this.node, super.get());
        }
        super.setChanged(false);
    }

    @Override
    public void setChanged(boolean changed) {
        DBStatusListener listener = DBStatusListenerManager.getListener(this.column.getDbName() + ":" + this.column.getTableName());
        if (listener != null) {
            listener.changed(null, null, null);
        }
        // 重新格式化值
        if (!changed && this.node instanceof BinaryTextFiled filed) {
            filed.setText(BinaryTextFiled.format(filed.getValue(), filed.getScale()));
        }
        this.setToNullFlag = false;
        super.setChanged(changed);
    }

    /**
     * 更新原始值
     */
    public void updateOriginal() {
        try {
            if (this.node != null) {
                super.set(ShellSqliteNodeUtil.getNodeVal(this.node));
            }
            this.original = super.get();
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    /**
     * 复制为insert语句
     */
    public void vCopyAsInsertSql() {
        SQLiteColumns columns = this.record.getColumns();
        String sql = ShellSqliteDataUtil.toInsertSql(columns, this.record, true);
        ClipboardUtil.copy(sql);
    }

    /**
     * 复制为update语句
     */
    public void vCopyAsUpdateSql() {
        SQLiteColumns columns = this.record.getColumns();
        String sql = ShellSqliteDataUtil.toUpdateSql(columns, this.record);
        ClipboardUtil.copy(sql);
    }

    @Override
    public SQLiteColumn getColumn() {
        return (SQLiteColumn) super.getColumn();
    }

    /**
     * 设置字段
     *
     * @param column 字段
     */
    public void setColumn(SQLiteColumn column) {
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
