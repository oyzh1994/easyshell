package cn.oyzh.easyshell.trees.sqlserver;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * SqlServer数据库树节点基类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public abstract class ShellSqlServerTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    /**
     * 构造SqlServer数据库树节点
     *
     * @param treeView 树视图
     */
    public ShellSqlServerTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellSqlServerTreeView getTreeView() {
        return (ShellSqlServerTreeView) super.getTreeView();
    }
}
