package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import javafx.scene.control.TreeItem;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class RedisMoreTreeItem extends RichTreeItem<RedisMoreTreeItemValue> {

    public RedisMoreTreeItem(RedisKeyTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(false);
        this.setValue(new RedisMoreTreeItemValue());
    }

    @Override
    public RedisDatabaseTreeItem parent() {
        TreeItem<?> parent = this.getParent();
        return (RedisDatabaseTreeItem) parent;
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoading()) {
            RedisDatabaseTreeItem treeItem = this.parent();
            if (treeItem != null) {
                treeItem.loadChild();
            }
        }
    }

}
