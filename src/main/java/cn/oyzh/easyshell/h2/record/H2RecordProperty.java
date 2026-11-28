package cn.oyzh.easyshell.h2.record;

import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.easyshell.util.h2.ShellH2DataUtil;
import cn.oyzh.easyshell.util.h2.ShellH2NodeUtil;
import cn.oyzh.easyshell.util.h2.ShellH2RecordUtil;
import cn.oyzh.fx.db.DBRecordProperty;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;

/**
 * H2记录属性
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2RecordProperty extends DBRecordProperty {

    /**
     * 表记录
     */
    private H2Record record;

    /**
     * 构造记录属性
     *
     * @param record   记录
     * @param column   字段
     * @param value    值
     * @param readonly 是否只读
     */
    public H2RecordProperty(H2Record record, H2Column column, Object value, boolean readonly) {
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
        if (this.isReadonly() || !this.isChanged() || this.node == null) {
            return super.get();
        }
        if (this.setToNullFlag) {
            return null;
        }
        try {
            return ShellH2NodeUtil.getNodeVal(this.node);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    @Override
    public void set(Object newValue) {
        super.set(newValue);
        if (this.node != null) {
            ShellH2NodeUtil.setNodeVal(this.node, newValue);
        }
    }

    @Override
    public Object getValue() {
        if (this.isReadonly()) {
            return ShellH2RecordUtil.formatValue(super.getValue(), this.getColumn());
        }
        if (this.node == null) {
            this.node = ShellH2RecordUtil.getNode(this, super.get(), this.getColumn());
            TableViewUtil.rowOnCtrlS(this.node);
            TableViewUtil.selectRowOnMouseClicked(this.node);
        }
        return this.node;
    }

    @Override
    public boolean isReadonly() {
        // 生成列由数据库计算；查询结果字段在解析后可能才补充 generated 标记，因此动态判断
        return super.isReadonly() || (this.column != null && this.column.isGenerated());
    }

    @Override
    public void discard() {
        if (this.isChanged() && this.node != null) {
            ShellH2NodeUtil.setNodeVal(this.node, super.get());
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

    @Override
    public void updateOriginal() {
        try {
            if (this.isChanged() && this.node != null) {
                super.set(ShellH2NodeUtil.getNodeVal(this.node));
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
        H2Columns columns = this.record.getColumns();
        String sql = ShellH2DataUtil.toInsertSql(columns, this.record, true);
        ClipboardUtil.copy(sql);
    }

    /**
     * 复制为update语句
     */
    public void vCopyAsUpdateSql() {
        H2Columns columns = this.record.getColumns();
        String sql = ShellH2DataUtil.toUpdateSql(columns, this.record);
        ClipboardUtil.copy(sql);
    }

    @Override
    public H2Column getColumn() {
        return (H2Column) super.getColumn();
    }

    /**
     * 设置字段
     *
     * @param column 字段
     */
    public void setColumn(H2Column column) {
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
