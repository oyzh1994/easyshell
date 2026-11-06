package cn.oyzh.easyshell.trees.postgresql;


import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * PostgreSQL数据库树节点基类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public abstract class ShellPostgresqlTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    /**
     * 构造PostgreSQL数据库树节点
     *
     * @param treeView 树视图
     */
    public ShellPostgresqlTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellPostgresqlTreeView getTreeView() {
        return (ShellPostgresqlTreeView) super.getTreeView();
    }
}
