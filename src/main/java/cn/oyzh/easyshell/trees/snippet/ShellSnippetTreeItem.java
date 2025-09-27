package cn.oyzh.easyshell.trees.snippet;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSnippet;
import cn.oyzh.easyshell.store.ShellSnippetStore;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.MenuItem;

import java.util.List;
import java.util.Objects;

/**
 * shell片段节点
 *
 * @author oyzh
 * @since 2025/06/11
 */
public class ShellSnippetTreeItem extends RichTreeItem<ShellSnippetTreeItemValue> {

    /**
     * shell片段储存
     */
    private final ShellSnippetStore snippetStore = ShellSnippetStore.INSTANCE;

    /**
     * shell片段
     */
    private ShellSnippet value;

    public ShellSnippet value() {
        return value;
    }

    public ShellSnippetTreeItem(ShellSnippet value, RichTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        this.value(value);
    }

    @Override
    public ShellSnippetTreeView getTreeView() {
        return (ShellSnippetTreeView) super.getTreeView();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = this.getTreeView().getMenuItems();
        FXMenuItem edit = MenuItemHelper.editSnippet("12", this::edit);
        items.add(edit);
        FXMenuItem rename = MenuItemHelper.renameSnippet("12", this::rename);
        items.add(rename);
        FXMenuItem delete = MenuItemHelper.deleteSnippet("12", this::delete);
        items.add(delete);
        return items;
    }

    @Override
    public void delete() {
        if (MessageBox.confirm(I18nHelper.delete() + " [" + this.value().getName() + "]")) {
            if (this.snippetStore.delete(this.value)) {
                this.getTreeView().deleteSnippet(this.value);
                this.remove();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        }
    }

    /**
     * 编辑
     */
    private void edit() {
        this.getTreeView().editSnippet(this.value);
    }

    @Override
    public void rename() {
        String connectName = MessageBox.prompt(I18nHelper.contentTip1(), this.value.getName());
        // 名称为null或者跟当前名称相同，则忽略
        if (connectName == null || Objects.equals(connectName, this.value.getName())) {
            return;
        }
        // 检查名称
        if (StringUtil.isBlank(connectName)) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty());
            return;
        }
        this.value.setName(connectName);
        // 修改名称
        if (this.snippetStore.update(this.value)) {
            this.setValue(new ShellSnippetTreeItemValue(this));
        } else {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    /**
     * 设置值
     *
     * @param value ssh信息
     */
    public void value(ShellSnippet value) {
        this.value = value;
        super.setValue(new ShellSnippetTreeItemValue(this));
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.getTreeView().editSnippet(this.value);
    }

    public String snippetName() {
        return this.value.getName();
    }

    public String getId() {
        return this.value.getId();
    }

    /**
     * 未保存属性
     */
    private final BooleanProperty unsaved = new SimpleBooleanProperty(false);

    public BooleanProperty unsavedProperty() {
        return unsaved;
    }

    public void setUnsaved(boolean unsaved) {
        this.unsaved.set(unsaved);
    }

    public  boolean isUnsaved() {
        return this.unsaved.get();
    }
}
