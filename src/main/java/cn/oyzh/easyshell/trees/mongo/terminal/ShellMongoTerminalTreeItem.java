package cn.oyzh.easyshell.trees.mongo.terminal;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * mongodb终端节点
 *
 * @author oyzh
 * @since 2023/1/30
 */
public class ShellMongoTerminalTreeItem extends RichTreeItem<ShellMongoTerminalTreeItemValue> {

    /**
     * 构造终端节点
     *
     * @param treeView 树视图
     */
    public ShellMongoTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellMongoTerminalTreeItemValue());
    }

    /**
     * 获取父节点
     *
     * @return 父节点
     */
    public ShellMongoDatabaseTreeItem parent() {
        return (ShellMongoDatabaseTreeItem) super.parent();
    }

    /**
     * 获取shell连接信息
     *
     * @return shell连接信息
     */
    public ShellConnect shellConnect() {
        return this.parent().shellConnect();
    }

    /**
     * 获取mongodb客户端
     *
     * @return mongodb客户端
     */
    public ShellMongoClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMongoEventUtil.terminalOpen(this.client(), this.parent().dbName());
    }

}
