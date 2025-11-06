package cn.oyzh.easyshell.trees.mysql;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * 基础的树节点
 *
 * @author oyzh
 * @since 2023/06/27
 */
public abstract class DBTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    public DBTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public DBTreeView getTreeView() {
        return (DBTreeView) super.getTreeView();
    }
}
