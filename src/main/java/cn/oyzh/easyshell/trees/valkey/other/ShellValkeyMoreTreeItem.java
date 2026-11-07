package cn.oyzh.easyshell.trees.valkey.other;

import cn.oyzh.easyshell.trees.valkey.ShellValkeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.ShellValkeyTreeView;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyKeysTreeItem;
import javafx.scene.control.TreeItem;

/**
 * redis加载更多节点
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyMoreTreeItem extends ShellValkeyTreeItem<ShellValkeyMoreTreeItemValue> {

    /**
     * 构造加载更多节点
     *
     * @param treeView 树视图
     */
    public ShellValkeyMoreTreeItem(ShellValkeyTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(false);
        this.setValue(new ShellValkeyMoreTreeItemValue());
    }

    @Override
    public ShellValkeyKeysTreeItem parent() {
        TreeItem<?> parent = this.getParent();
        return (ShellValkeyKeysTreeItem) parent;
    }

    @Override
    public void onPrimaryDoubleClick() {
        //        if (!this.isLoading()) {
        ShellValkeyKeysTreeItem treeItem = this.parent();
        if (treeItem != null) {
            treeItem.loadChild();
        }
        //        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ShellValkeyMoreTreeItem) {
            return 0;
        }
        return 1;
    }

}
