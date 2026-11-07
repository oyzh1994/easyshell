package cn.oyzh.easyshell.trees.valkey;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * 基础树节点
 *
 * @author oyzh
 * @since 2026-06-29
 */
public abstract class ShellValkeyTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    /**
     * 构造redis树节点
     *
     * @param treeView 树视图
     */
    public ShellValkeyTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellValkeyTreeView getTreeView() {
        return (ShellValkeyTreeView) super.getTreeView();
    }
}
