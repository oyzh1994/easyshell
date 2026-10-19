package cn.oyzh.easyshell.trees.mariadb.terminal;

import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * MariaDB终端树节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTerminalTreeItem extends RichTreeItem<ShellMariadbTerminalTreeItemValue> {

    /**
     * 构造终端节点
     *
     * @param treeView 树视图
     */
    public ShellMariadbTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellMariadbTerminalTreeItemValue());
    }

    /**
     * 获取父节点
     *
     * @return 父节点
     */
    public ShellMariadbDatabaseTreeItem parent() {
        return (ShellMariadbDatabaseTreeItem) super.parent();
    }

    /**
     * 获取MariaDB客户端
     *
     * @return MariaDB客户端
     */
    public ShellMariadbClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMariadbEventUtil.terminalOpen(this.parent());
    }

}
