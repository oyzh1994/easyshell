package cn.oyzh.easyshell.trees.redis;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * 基础树节点
 *
 * @author oyzh
 * @since 2023/06/27
 */
public abstract class ShellRedisTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    public ShellRedisTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellRedisTreeView getTreeView() {
        return (ShellRedisTreeView) super.getTreeView();
    }
}
