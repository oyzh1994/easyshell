package cn.oyzh.easyshell.trees.mongo.terminal;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mongo.MongoEventUtil;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class MongoTerminalTreeItem extends RichTreeItem<MongoTerminalTreeItemValue> {

    public MongoTerminalTreeItem(RichTreeView treeView) {
        super(treeView);
        this.setValue(new MongoTerminalTreeItemValue());
    }

    public MongoDatabaseTreeItem parent() {
        return (MongoDatabaseTreeItem) super.parent();
    }

    public ShellConnect shellConnect() {
        return this.parent().shellConnect();
    }

    public ShellMongoClient client() {
        return this.parent().client();
    }

    @Override
    public void onPrimaryDoubleClick() {
        MongoEventUtil.terminalOpen(this.client(), this.parent().dbName());
    }

}
