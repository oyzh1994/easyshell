package cn.oyzh.easyshell.trees.zk.other;

import cn.oyzh.easyshell.trees.zk.ShellZKTreeItem;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.trees.zk.ShellZKTreeView;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import javafx.scene.control.TreeItem;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class ShellZKMoreTreeItem extends ShellZKTreeItem<ShellZKMoreTreeItemValue> {

    public ShellZKMoreTreeItem(ShellZKTreeView treeView) {
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
        ShellZKNodeTreeItem treeItem = this.parent();
        if (treeItem != null) {
            treeItem.loadChild();
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ShellZKMoreTreeItem) {
            return 0;
        }
        return 1;
    }
}
