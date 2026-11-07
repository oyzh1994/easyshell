package cn.oyzh.easyshell.trees.redis.other;

import cn.oyzh.easyshell.trees.redis.ShellRedisTreeItem;
import cn.oyzh.easyshell.trees.redis.ShellRedisTreeView;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeysTreeItem;
import javafx.scene.control.TreeItem;

/**
 * redis加载更多节点
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisMoreTreeItem extends ShellRedisTreeItem<ShellRedisMoreTreeItemValue> {

    /**
     * 构造加载更多节点
     *
     * @param treeView 树视图
     */
    public ShellRedisMoreTreeItem(ShellRedisTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(false);
        this.setValue(new ShellRedisMoreTreeItemValue());
    }

    @Override
    public ShellRedisKeysTreeItem parent() {
        TreeItem<?> parent = this.getParent();
        return (ShellRedisKeysTreeItem) parent;
    }

    @Override
    public void onPrimaryDoubleClick() {
        //        if (!this.isLoading()) {
        ShellRedisKeysTreeItem treeItem = this.parent();
        if (treeItem != null) {
            treeItem.loadChild();
        }
        //        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ShellRedisMoreTreeItem) {
            return 0;
        }
        return 1;
    }

}
