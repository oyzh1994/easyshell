package cn.oyzh.easyshell.event.mongo.user;

import cn.oyzh.easyshell.mongo.user.MongoUser;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mongodb用户查看事件
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoUserViewEvent extends Event<MongoUser> {

    /**
     * 数据库节点
     */
    private ShellMongoDatabaseTreeItem dbItem;

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.dbItem.dbName();
    }

    public ShellMongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
