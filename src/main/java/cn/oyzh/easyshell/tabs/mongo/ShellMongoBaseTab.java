package cn.oyzh.easyshell.tabs.mongo;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * @author oyzh
 * @since 2024-09-12
 */
public abstract class ShellMongoBaseTab extends RichTab {

    public abstract ShellMongoDatabaseTreeItem dbItem() ;
}
