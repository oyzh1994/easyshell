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

    /**
     * 构造终端节点
     *
     * @param treeView 树视图
     */
    public ShellMysqlTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellMysqlTerminalTreeItemValue());
    }

    /**
     * 获取父节点
     *
     * @return 父节点
     */
    public ShellMysqlDatabaseTreeItem parent() {
        return (ShellMysqlDatabaseTreeItem) super.parent();
    }

    /**
     * 获取mysql客户端
     *
     * @return mysql客户端
     */
    public ShellMysqlClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMysqlEventUtil.terminalOpen(this.parent());
    }

}
