package cn.oyzh.easyshell.mysql.record;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.listener.DBStatusListener;
import cn.oyzh.easyshell.db.listener.DBStatusListenerManager;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.util.mysql.ShellMysqlDataUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlNodeUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlRecordUtil;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;

/**
 * db表记录属性
 *
 * @author oyzh
 * @since 2024/01/31
 */
public class MysqlRecordProperty extends SimpleObjectProperty<Object> implements Destroyable {

    /**
     * 是否变更
     */
    private SimpleBooleanProperty changedProperty;

    /**
     * 表字段
     */
    private MysqlColumn column;

    // /**
    //  * 表字段列表
    //  */
    // private MysqlColumns columns;

    /**
     * 表记录
     */
    private MysqlRecord record;

    /**
     * 原始数据
     */
    private Object original;

    /**
     * 设置为null标志位
     */
    private boolean setToNullFlag;

    /**
     * 只读模式
     */
    private final boolean readonly;

    // public MysqlRecordProperty(MysqlColumn column, Object value) {
    //     this(column, value, false);
    // }

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

    // public MysqlRecordProperty(MysqlRecord record, MysqlColumn column, Object value, boolean readonly) {
    //     super(value);
    //     this.record = record;
    //     this.column = column;
    //     if (!readonly) {
    //         this.original = value;
    //     }
    //     this.readonly = readonly;
    // }

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

    /**
     * 节点
     */
    private Node node;

    // private static LongAdder adder = new LongAdder();

    @Override
    public Object getValue() {
        if (this.readonly) {
//        if (this.readonly || !this.record.isEditable()) {
            return ShellMysqlRecordUtil.formatValue(super.getValue(), this.column);
        }
        if (this.node == null) {
            this.node = ShellMysqlRecordUtil.getNode(this, super.get(), this.column);
            TableViewUtil.rowOnCtrlS(this.node);
            TableViewUtil.selectRowOnMouseClicked(this.node);
            // adder.increment();
            // System.out.println("adder:" + adder.longValue());
        }
        return this.node;
    }

    /**
     * 抛弃
     */
    public void discard() {
        if (this.isChanged() && this.node != null) {
            ShellMysqlNodeUtil.setNodeVal(this.node, super.get());
        }
        this.setChanged(false);
    }

    public SimpleBooleanProperty changedProperty() {
        if (this.changedProperty == null) {
            this.changedProperty = new SimpleBooleanProperty();
        }
        return this.changedProperty;
    }

    public boolean isChanged() {
        return this.changedProperty != null && this.changedProperty.get();
    }

    public void setChanged(boolean changed) {
        this.changedProperty().set(changed);
        DBStatusListener listener;
        if (this.column.getSchema() != null) {
            listener = DBStatusListenerManager.getListener(this.column.getDbName() + ":" + this.column.getSchema() + ":" + this.column.getTableName());
        } else {
            listener = DBStatusListenerManager.getListener(this.column.getDbName() + ":" + this.column.getTableName());
        }
        if (listener != null) {
            listener.changed(null, null, null);
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

    public Node getControl() {
        return this.node;
    }

    public void vCopy() {
        ClipboardUtil.copy(this.node);
    }

    public void vPaste() {
        ClipboardUtil.paste(this.node);
    }

    // public void vDelete() {
    //     ShellMysqlEventUtil.recordDelete(this.record);
    // }
    //
    // /**
    //  * 转换为字段列表
    //  *
    //  * @return 字段列表
    //  */
    // private MysqlColumns toColumns() {
    //     Set<String> cols = this.record.columns();
    //     MysqlColumns columns = new MysqlColumns();
    //     int pos = 0;
    //     for (String col : cols) {
    //         MysqlColumn column = new MysqlColumn(col);
    //         column.setPosition(pos++);
    //         column.setTableName(this.column.getTableName());
    //         columns.add(column);
    //     }
    //     return columns;
    // }

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

    public void vSetToNull() {
        if (this.node instanceof TextField textField) {
            // 如果内容为空，则直接设置变更
            if (StringUtil.isEmpty(textField.getText())) {
                this.setChanged(true);
            } else {
                textField.clear();
            }
            textField.setPromptText(ShellMysqlRecordUtil.nullPromptText());
            NodeUtil.unFocus(this.node);
        }
        this.setToNullFlag = true;
    }

    public void vSetToEmptyString() {
        if (this.node instanceof TextField textField) {
            // 如果内容为空，则直接设置变更
            if (StringUtil.isEmpty(textField.getText())) {
                this.setChanged(true);
            } else {
                textField.setText("");
            }
            textField.setPromptText("");
            NodeUtil.unFocus(this.node);
        }
    }

    public MysqlColumn getColumn() {
        return column;
    }

    public void setColumn(MysqlColumn column) {
        this.column = column;
    }

    public Object getOriginal() {
        return original;
    }

    public void setOriginal(Object original) {
        this.original = original;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public void destroy() {
        if (this.node != null) {
            NodeDestroyUtil.destroyObject(this.node);
            this.node = null;
            this.column = null;
            this.record = null;
            this.original = null;
            this.changedProperty = null;
            // adder.decrement();
            // System.out.println("adder:" + adder.longValue());
        }
    }
}
