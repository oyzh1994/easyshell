package cn.oyzh.easyshell.trees.sqlite;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * SQLite树节点基类
 *
 * @author oyzh
 * @since 2026-11-02
 */
public abstract class ShellSqliteTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    /**
     * 构造SQLite树节点
     *
     * @param treeView 树视图
     */
    public ShellSqliteTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellSqliteTreeView getTreeView() {
        return (ShellSqliteTreeView) super.getTreeView();
    }
}
