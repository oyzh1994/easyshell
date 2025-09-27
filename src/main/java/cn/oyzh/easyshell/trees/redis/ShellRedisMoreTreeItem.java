package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import javafx.scene.control.TreeItem;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class ShellRedisMoreTreeItem extends RichTreeItem<ShellRedisMoreTreeItemValue> {

    public ShellRedisMoreTreeItem(ShellRedisKeyTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(false);
        this.setValue(new ShellRedisMoreTreeItemValue());
    }

    @Override
    public ShellRedisDatabaseTreeItem parent() {
        TreeItem<?> parent = this.getParent();
        return (ShellRedisDatabaseTreeItem) parent;
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoading()) {
            ShellRedisDatabaseTreeItem treeItem = this.parent();
            if (treeItem != null) {
                treeItem.loadChild();
            }
        }
    }

}
