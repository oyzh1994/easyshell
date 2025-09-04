package cn.oyzh.easyshell.trees.zk;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import javafx.scene.control.TreeItem;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class ZKMoreTreeItem extends RichTreeItem<ZKMoreTreeItemValue> {

    public ZKMoreTreeItem(ZKNodeTreeView treeView) {
        super(treeView);
        super.setSortable(false);
        super.setFilterable(false);
        this.setValue(new ZKMoreTreeItemValue());
    }

    @Override
    public ZKNodeTreeItem parent() {
        TreeItem<?> parent = this.getParent();
        return (ZKNodeTreeItem) parent;
    }

    @Override
    public void onPrimaryDoubleClick() {
//        if (!this.isLoading()) {
            ZKNodeTreeItem treeItem = this.parent();
            if (treeItem != null) {
                treeItem.loadChild();
            }
//        }
    }

}
