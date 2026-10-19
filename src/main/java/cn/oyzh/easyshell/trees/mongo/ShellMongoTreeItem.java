package cn.oyzh.easyshell.trees.mongo;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * 基础的树节点
 *
 * @author oyzh
 * @since 2026-06-29
 */
public abstract class ShellMongoTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    /**
     * 构造mongodb树节点
     *
     * @param treeView 树视图
     */
    public ShellMongoTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellMongoTreeView getTreeView() {
        return (ShellMongoTreeView) super.getTreeView();
    }
}
