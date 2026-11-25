package cn.oyzh.easyshell.event.mongo.view;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.view.ShellMongoViewTreeItem;
import cn.oyzh.event.Event;

/**
 * MongoDB 视图打开事件。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoViewOpenEvent extends Event<ShellMongoViewTreeItem> {

    private ShellMongoDatabaseTreeItem dbItem;

    public String viewName() {
        return this.data().viewName();
    }

    public ShellMongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
