package cn.oyzh.easyshell.trees.sqlserver.terminal;

import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.event.sqlserver.ShellSqlServerEventUtil;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * SqlServer数据库树终端节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerTerminalTreeItem extends RichTreeItem<ShellSqlServerTerminalTreeItemValue> {

    /**
     * 构造SqlServer数据库树终端节点
     *
     * @param treeView 树视图
     */
    public ShellSqlServerTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellSqlServerTerminalTreeItemValue());
    }

    /**
     * 获取所属模式节点
     *
     * @return 模式节点
     */
    public ShellSqlServerSchemaTreeItem parent() {
        return (ShellSqlServerSchemaTreeItem) super.parent();
    }

    /**
     * 获取SqlServer数据库客户端
     *
     * @return SqlServer数据库客户端
     */
    public ShellSqlServerClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellSqlServerEventUtil.terminalOpen(this.parent());
    }

}
