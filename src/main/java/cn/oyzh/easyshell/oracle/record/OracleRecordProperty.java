package cn.oyzh.easyshell.oracle.record;

import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleNodeUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleRecordUtil;
import cn.oyzh.fx.db.DBRecordProperty;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;

/**
 * Oracle记录属性
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleRecordProperty extends DBRecordProperty {

    /**
     * 表记录
     */
    private OracleRecord record;

    /**
     * 构造记录属性
     *
     * @param record   记录
     * @param column   字段
     * @param value    值
     * @param readonly 是否只读
     */
    public OracleRecordProperty(OracleRecord record, OracleColumn column, Object value, boolean readonly) {
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
            return ShellOracleNodeUtil.getNodeVal(this.node);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    @Override
    public void set(Object newValue) {
        super.set(newValue);
        if (this.node != null) {
            ShellOracleNodeUtil.setNodeVal(node, newValue);
        }
    }

    @Override
    public Object getValue() {
        if (this.readonly) {
            return ShellOracleRecordUtil.formatValue(super.getValue(), this.getColumn());
        }
        if (this.node == null) {
            this.node = ShellOracleRecordUtil.getNode(this, super.getValue(), this.getColumn());
            TableViewUtil.rowOnCtrlS(this.node);
            TableViewUtil.selectRowOnMouseClicked(this.node);
        }
        return this.node;
    }

    @Override
    public void discard() {
        if (this.isChanged() && this.node != null) {
            ShellOracleNodeUtil.setNodeVal(this.node, super.get());
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
                super.set(ShellOracleNodeUtil.getNodeVal(this.node));
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
        OracleColumns columns = this.record.getColumns();
        String sql = ShellOracleDataUtil.toInsertSql(columns, this.record, true);
        ClipboardUtil.copy(sql);
    }

    /**
     * 复制为update语句
     */
    public void vCopyAsUpdateSql() {
        OracleColumns columns = this.record.getColumns();
        String sql = ShellOracleDataUtil.toUpdateSql(columns, this.record);
        ClipboardUtil.copy(sql);
    }

   @Override
    public OracleColumn getColumn() {
        return (OracleColumn) super.getColumn();
    }

    /**
     * 设置字段
     *
     * @param column 字段
     */
    public void setColumn(OracleColumn column) {
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
