package cn.oyzh.easyshell.mariadb.record;

import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbDataUtil;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbNodeUtil;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbRecordUtil;
import cn.oyzh.fx.db.DBRecordProperty;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;

/**
 * MariaDB记录属性
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbRecordProperty extends DBRecordProperty {

    /**
     * 表字段
     */
    private MariadbColumn column;

    /**
     * 表记录
     */
    private MariadbRecord record;

    /**
     * 构造记录属性
     *
     * @param record   记录
     * @param column   字段
     * @param value    值
     * @param readonly 是否只读
     */
    public MariadbRecordProperty(MariadbRecord record, MariadbColumn column, Object value, boolean readonly) {
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
            return ShellMariadbNodeUtil.getNodeVal(this.node);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    @Override
    public void set(Object newValue) {
        super.set(newValue);
        if (this.node != null) {
            ShellMariadbNodeUtil.setNodeVal(node, newValue);
        }
    }

    @Override
    public Object getValue() {
        if (this.readonly) {
            return ShellMariadbRecordUtil.formatValue(super.getValue(), this.column);
        }
        if (this.node == null) {
            this.node = ShellMariadbRecordUtil.getNode(this, super.get(), this.column);
            TableViewUtil.rowOnCtrlS(this.node);
            TableViewUtil.selectRowOnMouseClicked(this.node);
        }
        return this.node;
    }

    @Override
    public void discard() {
        if (this.isChanged() && this.node != null) {
            ShellMariadbNodeUtil.setNodeVal(this.node, super.get());
        }
        super.discard();
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
                super.set(ShellMariadbNodeUtil.getNodeVal(this.node));
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
        MariadbColumns columns = this.record.getColumns();
        String sql = ShellMariadbDataUtil.toInsertSql(columns, this.record, true);
        ClipboardUtil.copy(sql);
    }

    /**
     * 复制为update语句
     */
    public void vCopyAsUpdateSql() {
        MariadbColumns columns = this.record.getColumns();
        String sql = ShellMariadbDataUtil.toUpdateSql(columns, this.record);
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

    /**
     * 获取字段
     *
     * @return 字段
     */
    public MariadbColumn getColumn() {
        return column;
    }

    /**
     * 设置字段
     *
     * @param column 字段
     */
    public void setColumn(MariadbColumn column) {
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
