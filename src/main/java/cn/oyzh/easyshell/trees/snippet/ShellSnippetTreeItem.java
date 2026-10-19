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
 * @since 2025-06-11
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

    /**
     * 获取片段对象
     *
     * @return 片段对象
     */
    public ShellSnippet value() {
        return value;
    }

    /**
     * 构造片段节点
     *
     * @param value    片段对象
     * @param treeView 树视图
     */
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
        FXMenuItem edit = MenuItemHelper.editSnippet( this::edit);
        items.add(edit);
        FXMenuItem rename = MenuItemHelper.renameSnippet( this::rename);
        items.add(rename);
        FXMenuItem delete = MenuItemHelper.deleteSnippet( this::delete);
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
     * @param value 片段对象
     */
    public void value(ShellSnippet value) {
        this.value = value;
        super.setValue(new ShellSnippetTreeItemValue(this));
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.getTreeView().editSnippet(this.value);
    }

    /**
     * 获取片段名称
     *
     * @return 片段名称
     */
    public String snippetName() {
        return this.value.getName();
    }

    /**
     * 获取片段唯一标识
     *
     * @return 片段唯一标识
     */
    public String getId() {
        return this.value.getId();
    }

    /**
     * 未保存属性
     */
    private final BooleanProperty unsaved = new SimpleBooleanProperty(false);

    /**
     * 获取未保存属性
     *
     * @return 未保存属性
     */
    public BooleanProperty unsavedProperty() {
        return unsaved;
    }

    /**
     * 设置是否未保存
     *
     * @param unsaved 是否未保存
     */
    public void setUnsaved(boolean unsaved) {
        this.unsaved.set(unsaved);
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
    public  boolean isUnsaved() {
        return this.unsaved.get();
    }
}
