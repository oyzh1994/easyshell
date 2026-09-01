package cn.oyzh.easyshell.mysql.record;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.listener.DBStatusListener;
import cn.oyzh.easyshell.data.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.db.DBRecordProperty;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.util.mysql.ShellMysqlDataUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlNodeUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlRecordUtil;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;

/**
 * db表记录属性
 *
 * @author oyzh
 * @since 2024/01/31
 */
public class MysqlRecordProperty extends DBRecordProperty {

    /**
     * 表字段
     */
    private MysqlColumn column;

    /**
     * 表记录
     */
    private MysqlRecord record;

    public MysqlRecordProperty(MysqlRecord record, MysqlColumn column, Object value, boolean readonly) {
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
            return ShellMysqlNodeUtil.getNodeVal(this.node);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    @Override
    public void set(Object newValue) {
        super.set(newValue);
        if (this.node != null) {
            ShellMysqlNodeUtil.setNodeVal(node, newValue);
        }
    }

    @Override
    public Object getValue() {
        if (this.readonly) {
            return ShellMysqlRecordUtil.formatValue(super.getValue(), this.column);
        }
        if (this.node == null) {
            this.node = ShellMysqlRecordUtil.getNode(this, super.get(), this.column);
            TableViewUtil.rowOnCtrlS(this.node);
            TableViewUtil.selectRowOnMouseClicked(this.node);
        }
        return this.node;
    }

    @Override
    public void discard() {
        if (this.isChanged() && this.node != null) {
            ShellMysqlNodeUtil.setNodeVal(this.node, super.get());
        }
        super.discard();
    }

    @Override
    public void setChanged(boolean changed) {
        super.setChanged(changed);
        DBStatusListener listener;
        if (this.column.getSchema() != null) {
            listener = DBStatusListenerManager.getListener(this.column.getDbName() + ":" + this.column.getSchema() + ":" + this.column.getTableName());
        } else {
            listener = DBStatusListenerManager.getListener(this.column.getDbName() + ":" + this.column.getTableName());
        }
        if (listener != null) {
            listener.changed(null, null, null);
        }
        // 重新格式化值
        if (!changed && this.node instanceof BinaryTextFiled filed) {
            filed.setText(BinaryTextFiled.format(filed.getValue(), filed.getScale()));
        }
        this.setToNullFlag = false;
    }

    public void updateOriginal() {
        try {
            if (this.node != null) {
                super.set(ShellMysqlNodeUtil.getNodeVal(this.node));
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
        MysqlColumns columns = this.record.getColumns();
        String sql = ShellMysqlDataUtil.toInsertSql(columns, this.record, true);
        ClipboardUtil.copy(sql);
    }

    /**
     * 复制为update语句
     */
    public void vCopyAsUpdateSql() {
        MysqlColumns columns = this.record.getColumns();
        String sql = ShellMysqlDataUtil.toUpdateSql(columns, this.record);
        ClipboardUtil.copy(sql);
    }

    // public void vSetToNull() {
    //     if (this.node instanceof TextField textField) {
    //         // 如果内容为空，则直接设置变更
    //         if (StringUtil.isEmpty(textField.getText())) {
    //             this.setChanged(true);
    //         } else {
    //             textField.clear();
    //         }
    //         textField.setPromptText(ShellDBRecordUtil.nullPromptText());
    //         NodeUtil.unFocus(this.node);
    //     }
    //     this.setToNullFlag = true;
    // }
    //
    // public void vSetToEmptyString() {
    //     if (this.node instanceof TextField textField) {
    //         // 如果内容为空，则直接设置变更
    //         if (StringUtil.isEmpty(textField.getText())) {
    //             this.setChanged(true);
    //         } else {
    //             textField.setText("");
    //         }
    //         textField.setPromptText("");
    //         NodeUtil.unFocus(this.node);
    //     }
    // }

    public MysqlColumn getColumn() {
        return column;
    }

    public void setColumn(MysqlColumn column) {
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
