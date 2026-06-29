package cn.oyzh.easyshell.tabs.mongo;

import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * @author oyzh
 * @since 2024-09-12
 */
public abstract class MongoBaseTab extends RichTab {

    public abstract MongoDatabaseTreeItem dbItem() ;
}
