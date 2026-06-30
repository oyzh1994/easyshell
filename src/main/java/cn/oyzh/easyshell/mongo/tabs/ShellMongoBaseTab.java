package cn.oyzh.easyshell.mongo.tabs;

import cn.oyzh.easyshell.mongo.trees.database.MongoDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * @author oyzh
 * @since 2024-09-12
 */
public abstract class ShellMongoBaseTab extends RichTab {

    public abstract MongoDatabaseTreeItem dbItem() ;
}
