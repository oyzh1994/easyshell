package cn.oyzh.easyshell.trees.sqlite.terminal;

import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * SQLite终端树节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTerminalTreeItem extends RichTreeItem<ShellSqliteTerminalTreeItemValue> {

    /**
     * 构造终端节点
     *
     * @param treeView 树视图
     */
    public ShellSqliteTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellSqliteTerminalTreeItemValue());
    }

    /**
     * 获取父节点
     *
     * @return 父节点
     */
    @Override
    public ShellSqliteRootTreeItem parent() {
        return (ShellSqliteRootTreeItem) super.parent();
    }

    /**
     * 获取SQLite客户端
     *
     * @return SQLite客户端
     */
    public ShellSqliteClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellSqliteEventUtil.terminalOpen(this.client());
    }
}
