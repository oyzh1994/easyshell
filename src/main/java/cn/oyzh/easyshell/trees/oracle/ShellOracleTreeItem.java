package cn.oyzh.easyshell.trees.oracle;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * Oracle数据库树节点基类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public abstract class ShellOracleTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    /**
     * 构造Oracle数据库树节点
     *
     * @param treeView 树视图
     */
    public ShellOracleTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellOracleTreeView getTreeView() {
        return (ShellOracleTreeView) super.getTreeView();
    }
}
