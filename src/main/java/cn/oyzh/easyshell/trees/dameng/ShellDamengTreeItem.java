package cn.oyzh.easyshell.trees.dameng;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * 达梦数据库树节点基类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public abstract class ShellDamengTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    /**
     * 构造达梦数据库树节点
     *
     * @param treeView 树视图
     */
    public ShellDamengTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellDamengTreeView getTreeView() {
        return (ShellDamengTreeView) super.getTreeView();
    }
}
