package cn.oyzh.easyshell.trees.zk.other;

import cn.oyzh.easyshell.trees.zk.ShellZKTreeItem;
import cn.oyzh.easyshell.trees.zk.ShellZKTreeView;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import javafx.scene.control.TreeItem;

/**
 * zk返回上级节点
 *
 * @author oyzh
 * @since 2026-05-25
 */
public class ShellZKReturnTreeItem extends ShellZKTreeItem<ShellZKReturnTreeItemValue> {

    /**
     * 构造返回上级节点
     *
     * @param treeView 树视图
     */
    public ShellZKReturnTreeItem(ShellZKTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(false);
        this.setValue(new ShellZKReturnTreeItemValue());
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
            treeItem.loadPrent();
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ShellZKReturnTreeItem) {
            return 0;
        }
        return -1;
    }

}
