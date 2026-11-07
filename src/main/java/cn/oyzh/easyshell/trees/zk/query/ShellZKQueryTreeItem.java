package cn.oyzh.easyshell.trees.zk.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.zk.ShellZKTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * zk查询节点
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKQueryTreeItem extends ShellZKTreeItem<ShellZKQueryTreeItemValue> {

    /**
     * 当前查询
     */
    private final ShellQuery value;

    /**
     * 构造查询节点
     *
     * @param query    查询对象
     * @param treeView 树视图
     */
    public ShellZKQueryTreeItem(ShellQuery query, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = query;
        this.setValue(new ShellZKQueryTreeItemValue(this));
    }

    /**
     * 获取查询对象
     *
     * @return 查询对象
     */
    public ShellQuery value() {
        return value;
    }

    @Override
    public ShellZKQueriesTreeItem parent() {
        return (ShellZKQueriesTreeItem) super.parent();
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
     * 获取查询id
     *
     * @return 查询id
     */
    public String queryId() {
        return this.value.getUid();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem openQuery = MenuItemHelper.openQuery(this::onPrimaryDoubleClick);
        items.add(openQuery);
        FXMenuItem renameQuery = MenuItemHelper.renameQuery(this::rename);
        items.add(renameQuery);
        FXMenuItem deleteQuery = MenuItemHelper.deleteQuery(this::delete);
        items.add(deleteQuery);
        return items;
    }

    @Override
    public void delete() {
        if (MessageBox.confirm(ShellI18nHelper.confirm("delete", this.queryName()), this.getWindow())) {
            if (ShellQueryStore.INSTANCE.delete(this.value)) {
                this.getTreeView().closeQueryTab(this.queryId());
                this.parent().clearQuerySize();
                this.parent().refresh();
                this.remove();
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        }
    }

    @Override
    public void rename() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.queryName(), this.getWindow());
        // 名称为null或者跟当前名称相同，则忽略
        if (name == null || Objects.equals(name, this.queryName())) {
            return;
        }
        // 检查名称
        if (StringUtil.isBlank(name)) {
            MessageBox.warn(I18nHelper.pleaseInputName(), this.getWindow());
            return;
        }
        String oldName = this.value.getName();
        this.value.setName(name);
        // 修改名称
        if (ShellQueryStore.INSTANCE.update(this.value)) {
            this.getTreeView().renameQueryTab(this.queryId(), name);
            this.refresh();
        } else {
            this.value.setName(oldName);
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
        }
    }

    @Override
    public void onPrimaryDoubleClick() {
        this.getTreeView().openQuery(this);
    }
}
