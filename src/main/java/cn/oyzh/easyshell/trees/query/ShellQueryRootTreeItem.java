package cn.oyzh.easyshell.trees.query;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * shell查询根节点
 *
 * @author oyzh
 * @since 2025/06/11
 */
public class ShellQueryRootTreeItem extends RichTreeItem<ShellQueryRootTreeItemValue> {

    /**
     * shell查询储存
     */
    private final ShellQueryStore queryStore = ShellQueryStore.INSTANCE;

    public ShellQueryRootTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellQueryRootTreeItemValue());
        // // 加载子节点
        // this.loadChild();
    }

    @Override
    public ShellQueryTreeView getTreeView() {
        return (ShellQueryTreeView) super.getTreeView();
    }

    @Override
    public void reloadChild() {
        super.reloadChild();
        this.clearChild();
        this.loadChild();
    }

    @Override
    public void loadChild() {
        String iid = this.getTreeView().getIid();
        // 初始化查询
        List<ShellQuery> queries = this.queryStore.list(iid);
        if (CollectionUtil.isNotEmpty(queries)) {
            List<TreeItem<?>> list = new ArrayList<>();
            for (ShellQuery query : queries) {
                list.add(new ShellQueryTreeItem(query, this.getTreeView()));
            }
            this.addChild(list);
        }
        this.refresh();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        // List<MenuItem> items = new ArrayList<>(12);
        // FXMenuItem add = MenuItemHelper.add("12", this::add);
        // items.add(add);
        // return items;
        return this.getTreeView().getMenuItems();
    }

    // /**
    //  * 添加
    //  */
    // private void add() {
    //     this.getTreeView().addSnippet();
    // }
}
