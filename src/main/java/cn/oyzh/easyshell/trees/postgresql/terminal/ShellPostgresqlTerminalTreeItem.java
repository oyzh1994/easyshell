package cn.oyzh.easyshell.trees.postgresql.terminal;

import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.event.postgresql.ShellPostgresqlEventUtil;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * PostgreSQL数据库树终端节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlTerminalTreeItem extends RichTreeItem<ShellPostgresqlTerminalTreeItemValue> {

    /**
     * 构造PostgreSQL数据库树终端节点
     *
     * @param treeView 树视图
     */
    public ShellPostgresqlTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellPostgresqlTerminalTreeItemValue());
    }

    /**
     * 获取所属模式节点
     *
     * @return 模式节点
     */
    public ShellPostgresqlSchemaTreeItem parent() {
        return (ShellPostgresqlSchemaTreeItem) super.parent();
    }

    /**
     * 获取PostgreSQL数据库客户端
     *
     * @return PostgreSQL数据库客户端
     */
    public ShellPostgresqlClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellPostgresqlEventUtil.terminalOpen(this.parent());
    }

}
