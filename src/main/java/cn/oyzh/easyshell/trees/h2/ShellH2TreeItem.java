package cn.oyzh.easyshell.trees.h2;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * H2树节点基类
 *
 * @author oyzh
 * @since 2026-11-02
 */
public abstract class ShellH2TreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    /**
     * 构造H2树节点
     *
     * @param treeView 树视图
     */
    public ShellH2TreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellH2TreeView getTreeView() {
        return (ShellH2TreeView) super.getTreeView();
    }
}
