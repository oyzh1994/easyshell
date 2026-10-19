package cn.oyzh.easyshell.trees.mariadb;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * 基础树节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public abstract class ShellMariadbTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    /**
     * 构造MariaDB树节点
     *
     * @param treeView 树视图
     */
    public ShellMariadbTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellMariadbTreeView getTreeView() {
        return (ShellMariadbTreeView) super.getTreeView();
    }
}
