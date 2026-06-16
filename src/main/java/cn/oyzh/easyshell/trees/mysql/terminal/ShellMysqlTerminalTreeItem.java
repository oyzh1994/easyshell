package cn.oyzh.easyshell.trees.mysql.terminal;

import cn.oyzh.easyshell.event.mysql.ShellMysqlEventUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * mysql终端树节点
 *
 * @author oyzh
 * @since 2023/1/30
 */
public class ShellMysqlTerminalTreeItem extends RichTreeItem<ShellMysqlTerminalTreeItemValue> {

    public ShellMysqlTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellMysqlTerminalTreeItemValue());
    }

    public ShellMysqlDatabaseTreeItem parent() {
        return (ShellMysqlDatabaseTreeItem) super.parent();
    }

    public ShellMysqlClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMysqlEventUtil.terminalOpen(this.client(), this.parent().dbName());
    }

}
