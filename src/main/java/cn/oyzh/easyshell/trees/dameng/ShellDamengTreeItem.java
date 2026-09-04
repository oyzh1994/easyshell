package cn.oyzh.easyshell.trees.dameng;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * 基础的树节点
 *
 * @author oyzh
 * @since 2023/06/27
 */
public abstract class ShellDamengTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    public ShellDamengTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellDamengTreeView getTreeView() {
        return (ShellDamengTreeView) super.getTreeView();
    }
}
