package cn.oyzh.easyshell.trees.mongo.terminal;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class ShellMongoTerminalTreeItem extends RichTreeItem<ShellMongoTerminalTreeItemValue> {

    public ShellMongoTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new ShellMongoTerminalTreeItemValue());
    }

    public ShellMongoDatabaseTreeItem parent() {
        return (ShellMongoDatabaseTreeItem) super.parent();
    }

    public ShellConnect shellConnect() {
        return this.parent().shellConnect();
    }

    public ShellMongoClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMongoEventUtil.terminalOpen(this.client(), this.parent().dbName());
    }

}
