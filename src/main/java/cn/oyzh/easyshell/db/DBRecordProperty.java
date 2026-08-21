package cn.oyzh.easyshell.db;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

/**
 * db表记录属性
 *
 * @author oyzh
 * @since 2024/01/31
 */
public class DBRecordProperty extends SimpleObjectProperty<Object> implements Destroyable {

    /**
     * 节点
     */
    protected Node node;

    /**
     * 原始数据
     */
    protected Object original;

    /**
     * 设置为null标志位
     */
    protected boolean setToNullFlag;

    /**
     * 只读模式
     */
    protected boolean readonly;

    /**
     * 是否变更
     */
    protected SimpleBooleanProperty changedProperty;

    public DBRecordProperty() {
    }

    public DBRecordProperty(Object value) {
        super(value);
    }

    /**
     * 抛弃
     */
    public void discard() {
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
        return this.getControl();
    }

    @Override
    public void destroy() {
        if (this.node != null) {
            NodeDestroyUtil.destroyObject(this.node);
            this.node = null;
            this.original = null;
            this.changedProperty.unbind();
            this.changedProperty = null;
        }
    }
}
