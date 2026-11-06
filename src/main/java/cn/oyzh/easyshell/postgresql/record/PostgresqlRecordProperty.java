package cn.oyzh.easyshell.postgresql.record;

import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlDataUtil;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlNodeUtil;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlRecordUtil;
import cn.oyzh.fx.db.DBRecordProperty;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;

/**
 * PostgreSQL记录属性
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlRecordProperty extends DBRecordProperty {

    /**
     * 表记录
     */
    private PostgresqlRecord record;

    /**
     * 构造记录属性
     *
     * @param record   记录
     * @param column   字段
     * @param value    值
     * @param readonly 是否只读
     */
    public PostgresqlRecordProperty(PostgresqlRecord record, PostgresqlColumn column, Object value, boolean readonly) {
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
            return ShellPostgresqlNodeUtil.getNodeVal(this.node);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    @Override
    public void set(Object newValue) {
        super.set(newValue);
        if (this.node != null) {
            ShellPostgresqlNodeUtil.setNodeVal(node, newValue);
        }
    }

    @Override
    public Object getValue() {
        if (this.readonly) {
            return ShellPostgresqlRecordUtil.formatValue(super.getValue(), this.getColumn());
        }
        if (this.node == null) {
            this.node = ShellPostgresqlRecordUtil.getNode(this, super.getValue(), this.getColumn());
            TableViewUtil.rowOnCtrlS(this.node);
            TableViewUtil.selectRowOnMouseClicked(this.node);
        }
        return this.node;
    }

    @Override
    public void discard() {
        if (this.isChanged() && this.node != null) {
            ShellPostgresqlNodeUtil.setNodeVal(this.node, super.get());
        }
        super.setChanged(false);
    }

    @Override
    public void setChanged(boolean changed) {
        DBStatusListener listener = DBStatusListenerManager.getListener(this.column.getSchema() + ":" + this.column.getTableName());
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
                super.set(ShellPostgresqlNodeUtil.getNodeVal(this.node));
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
        PostgresqlColumns columns = this.record.getColumns();
        String sql = ShellPostgresqlDataUtil.toInsertSql(columns, this.record, true);
        ClipboardUtil.copy(sql);
    }

    /**
     * 复制为update语句
     */
    public void vCopyAsUpdateSql() {
        PostgresqlColumns columns = this.record.getColumns();
        String sql = ShellPostgresqlDataUtil.toUpdateSql(columns, this.record);
        ClipboardUtil.copy(sql);
    }

   @Override
    public PostgresqlColumn getColumn() {
        return (PostgresqlColumn) super.getColumn();
    }

    /**
     * 设置字段
     *
     * @param column 字段
     */
    public void setColumn(PostgresqlColumn column) {
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
