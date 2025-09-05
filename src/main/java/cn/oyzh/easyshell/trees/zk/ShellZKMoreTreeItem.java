package cn.oyzh.easyshell.trees.zk;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import javafx.scene.control.TreeItem;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class ShellZKMoreTreeItem extends RichTreeItem<ShellZKMoreTreeItemValue> {

    public ShellZKMoreTreeItem(ShellZKNodeTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(false);
        this.setValue(new ShellZKMoreTreeItemValue());
    }

    @Override
    public ShellZKNodeTreeItem parent() {
        TreeItem<?> parent = this.getParent();
        return (ShellZKNodeTreeItem) parent;
    }

    @Override
    public void onPrimaryDoubleClick() {
//        if (!this.isLoading()) {
            ShellZKNodeTreeItem treeItem = this.parent();
            if (treeItem != null) {
                treeItem.loadChild();
            }
//        }
    }

}
