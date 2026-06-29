package cn.oyzh.easyshell.trees.zk;


import cn.oyzh.easyshell.trees.redis.ShellRedisTreeView;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * 基础树节点
 *
 * @author oyzh
 * @since 2023/06/27
 */
public abstract class ShellZKTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    public ShellZKTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellZKTreeView getTreeView() {
        return (ShellZKTreeView) super.getTreeView();
    }
}
