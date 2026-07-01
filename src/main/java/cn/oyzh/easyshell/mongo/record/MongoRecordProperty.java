package cn.oyzh.easyshell.mongo.record;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.listener.DBStatusListener;
import cn.oyzh.easyshell.data.db.listener.DBStatusListenerManager;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.util.mongo.ShellMongoDataUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoNodeUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoRecordUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoUtil;
import cn.oyzh.fx.gui.text.field.BinaryTextFiled;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

/**
 * mongodb表记录属性
 *
 * @author oyzh
 * @since 2024/01/31
 */
public class MongoRecordProperty extends SimpleObjectProperty<Object> implements Destroyable {

    /**
     * 是否变更
     */
    private SimpleBooleanProperty changedProperty;

    /**
     * 表字段
     */
    private MongoColumn column;

    /**
     * 表记录
     */
    private MongoRecord record;

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

    public MongoRecordProperty(MongoRecord record, MongoColumn column, Object value, boolean readonly) {
        this.column = column;
        this.record = record;
        if (column.is_id()) {
            this.set(ShellMongoRecordUtil.idValue(value));
        } else {
            this.set(value);
        }
        if (!readonly || column.is_id()) {
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
            return ShellMongoNodeUtil.getNodeVal(this.node);
        } catch (Exception ex) {
            throw new ShellException(ex);
        }
    }

    @Override
    public void set(Object newValue) {
        // 旧数据
        Object oldVal = super.get();
        // 更新数据
        super.set(newValue);
        // 更新original值
        if (this.column.is_id() && this.original == null) {
            this.original = newValue;
        }
        // 变更标志
        if (oldVal != null && oldVal != newValue) {
            this.setChanged(true);
        }
        if (this.node != null) {
            String type = ShellMongoUtil.getType(newValue);
            if (StringUtil.notEquals(type, this.column.getType())) {
                this.column.setType(type);
                this.refreshNode();
            }
            Object value = this.column.is_id() ? ShellMongoRecordUtil.idValue(newValue) : newValue;
            ShellMongoNodeUtil.setNodeVal(this.node, value);
        }
    }

    /**
     * 节点
     */
    private Node node;

    @Override
    public Object getValue() {
        if (this.readonly) {
            return ShellMongoRecordUtil.formatValue(super.getValue(), this.column);
        }
        if (this.node == null) {
            this.initNode();
        }
        return this.node;
    }

    /**
     * 刷新节点
     */
    private void refreshNode() {
        this.node = null;
        this.initNode();
    }

    /**
     * 初始化节点
     */
    private void initNode() {
        this.node = ShellMongoRecordUtil.getNode(this, super.get(), this.column);
        TableViewUtil.rowOnCtrlS(this.node);
        TableViewUtil.selectRowOnMouseClicked(this.node);
    }

    /**
     * 抛弃
     */
    public void discard() {
        if (this.isChanged() && this.node != null) {
            ShellMongoNodeUtil.setNodeVal(this.node, super.get());
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
        DBStatusListener listener = DBStatusListenerManager.getListener(this.column.getDbName() + ":" + this.column.getCollectionName());
        if (listener != null) {
            listener.changed(null, null, null);
        }
        this.setToNullFlag = false;
        // 重新格式化值
        if (!changed && this.node instanceof BinaryTextFiled filed) {
            filed.setText(BinaryTextFiled.format(filed.getValue(), filed.getScale()));
        }
    }

    public void updateOriginal() {
        try {
            if (!this.column.is_id() && this.node != null) {
                super.set(ShellMongoNodeUtil.getNodeVal(this.node));
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

//    public void vEdit() {
//        StageAdapter adapter = ShellMongoViewFactory.documentUpdate(this.record);
//        if (adapter == null) {
//            return;
//        }
//        String doc = adapter.getProp("doc");
//        if (doc == null) {
//            return;
//        }
//        MongoRecord record = ShellMongoRecordUtil.docToRecord(doc, this.column.getDbName(), this.column.getCollectionName());
//        this.record.copy(record);
//    }

    /**
     * 复制为insert语句
     */
    public void vCopyAsInsertSql() {
        String sql = ShellMongoDataUtil.toInsertScript(this.record);
        ClipboardUtil.copy(sql);
    }

    /**
     * 复制为update语句
     */
    public void vCopyAsUpdateSql() {
        String sql = ShellMongoDataUtil.toUpdateScript(this.record);
        ClipboardUtil.copy(sql);
    }

    public void vSetToNull() {
        this.setChanged(true);
        this.setToNullFlag = true;
        ShellMongoNodeUtil.setToNullString(this.node);
    }

    public void vSetToEmptyString() {
        this.setChanged(true);
        ShellMongoNodeUtil.setToEmptyString(this.node);
    }

    public MongoColumn getColumn() {
        return column;
    }

    public void setColumn(MongoColumn column) {
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
    public synchronized void destroy() {
        if (this.node != null) {
            NodeDestroyUtil.destroyObject(this.node);
            this.node = null;
            this.column = null;
            this.record = null;
            this.original = null;
            this.changedProperty = null;
        }

    }
}
