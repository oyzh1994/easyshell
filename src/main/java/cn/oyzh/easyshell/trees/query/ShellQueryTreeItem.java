package cn.oyzh.easyshell.trees.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.store.ShellQueryStore;
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
 * shell查询节点
 *
 * @author oyzh
 * @since 2025/06/11
 */
public class ShellQueryTreeItem extends RichTreeItem<ShellQueryTreeItemValue> {

    /**
     * shell查询储存
     */
    private final ShellQueryStore queryStore = ShellQueryStore.INSTANCE;

    /**
     * shell查询
     */
    private ShellQuery value;

    /**
     * 获取查询对象
     *
     * @return 查询对象
     */
    public ShellQuery value() {
        return value;
    }

    /**
     * 构造查询节点
     *
     * @param value    查询对象
     * @param treeView 树视图
     */
    public ShellQueryTreeItem(ShellQuery value, RichTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        this.value(value);
    }

    @Override
    public ShellQueryTreeView getTreeView() {
        return (ShellQueryTreeView) super.getTreeView();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = this.getTreeView().getMenuItems();
        FXMenuItem edit = MenuItemHelper.editQuery( this::edit);
        items.add(edit);
        FXMenuItem rename = MenuItemHelper.renameQuery( this::rename);
        items.add(rename);
        FXMenuItem delete = MenuItemHelper.deleteQuery( this::delete);
        items.add(delete);
        return items;
    }

    @Override
    public void delete() {
        if (MessageBox.confirm(I18nHelper.delete() + " [" + this.value().getName() + "]")) {
            if (this.queryStore.delete(this.value)) {
                this.getTreeView().deleteQuery(this.value);
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
        this.getTreeView().editQuery(this.value);
    }

    @Override
    public void rename() {
        String newName = MessageBox.prompt(I18nHelper.contentTip1(), this.value.getName());
        // 名称为null或者跟当前名称相同，则忽略
        if (newName == null || Objects.equals(newName, this.value.getName())) {
            return;
        }
        // 检查名称
        if (StringUtil.isBlank(newName)) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty());
            return;
        }
        this.value.setName(newName);
        // 修改名称
        if (this.queryStore.update(this.value)) {
            this.setValue(new ShellQueryTreeItemValue(this));
        } else {
            MessageBox.warn(I18nHelper.operationFail());
        }
    }

    /**
     * 设置值
     *
     * @param value 查询对象
     */
    public void value(ShellQuery value) {
        this.value = value;
        super.setValue(new ShellQueryTreeItemValue(this));
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.getTreeView().editQuery(this.value);
    }

    /**
     * 获取查询名称
     *
     * @return 查询名称
     */
    public String queryName() {
        return this.value.getName();
    }

    /**
     * 获取查询唯一标识
     *
     * @return 查询唯一标识
     */
    public String getId() {
        return this.value.getUid();
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
