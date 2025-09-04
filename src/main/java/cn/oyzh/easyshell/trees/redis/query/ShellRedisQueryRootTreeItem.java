package cn.oyzh.easyshell.trees.redis.query;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.redis.ShellRedisQuery;
import cn.oyzh.easyshell.store.redis.RedisQueryStore;
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
public class ShellRedisQueryRootTreeItem extends RichTreeItem<ShellRedisQueryRootTreeItemValue> {

    /**
     * shell查询储存
     */
    private final RedisQueryStore queryStore = RedisQueryStore.INSTANCE;

    public ShellRedisQueryRootTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellRedisQueryRootTreeItemValue());
        // 加载子节点
        this.loadChild();
    }

    @Override
    public ShellRedisQueryTreeView getTreeView() {
        return (ShellRedisQueryTreeView) super.getTreeView();
    }

    @Override
    public void reloadChild() {
        super.reloadChild();
        this.clearChild();
        this.loadChild();
    }

    @Override
    public void loadChild() {
        // 初始化查询
        List<ShellRedisQuery> queries = this.queryStore.selectList();
        if (CollectionUtil.isNotEmpty(queries)) {
            List<TreeItem<?>> list = new ArrayList<>();
            for (ShellRedisQuery query : queries) {
                list.add(new ShellRedisQueryTreeItem(query, this.getTreeView()));
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
