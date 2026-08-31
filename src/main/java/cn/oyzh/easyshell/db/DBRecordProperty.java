package cn.oyzh.easyshell.db;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.db.ShellDBUtil;
import cn.oyzh.fx.plus.node.NodeDestroyUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
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

    /**
     * 设置为null字符串
     */
    public void vSetToNull() {
        if (this.node instanceof TextField textField) {
            // 如果内容为空，则直接设置变更
            if (StringUtil.isEmpty(textField.getText())) {
                this.setChanged(true);
            } else {
                textField.clear();
            }
            textField.setPromptText(ShellDBUtil.nullPromptText());
            NodeUtil.unFocus(this.node);
        }
        this.setToNullFlag = true;
    }

    /**
     * 设置为空字符串
     */
    public void vSetToEmptyString() {
        if (this.node instanceof TextField textField) {
            // 如果内容为空，则直接设置变更
            if (StringUtil.isEmpty(textField.getText())) {
                this.setChanged(true);
            }
            textField.setText("");
            textField.setPromptText("");
            NodeUtil.unFocus(this.node);
        }
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
