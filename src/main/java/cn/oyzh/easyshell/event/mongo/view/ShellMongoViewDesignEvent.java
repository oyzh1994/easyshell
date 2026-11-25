package cn.oyzh.easyshell.event.mongo.view;

import cn.oyzh.easyshell.mongo.view.MongoView;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * MongoDB 视图设计事件。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoViewDesignEvent extends Event<MongoView> {

    private ShellMongoDatabaseTreeItem dbItem;

    public String viewName() {
        return this.data().getName();
    }

    public ShellMongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
