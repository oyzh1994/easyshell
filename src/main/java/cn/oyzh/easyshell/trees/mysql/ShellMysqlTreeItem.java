package cn.oyzh.easyshell.trees.mysql;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * 基础树节点
 *
 * @author oyzh
 * @since 2025-11-06
 */
public abstract class ShellMysqlTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    /**
     * 构造mysql树节点
     *
     * @param treeView 树视图
     */
    public ShellMysqlTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellMysqlTreeView getTreeView() {
        return (ShellMysqlTreeView) super.getTreeView();
    }
}
