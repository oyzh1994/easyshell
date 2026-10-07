package cn.oyzh.easyshell.event.mongo.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mongodb查询打开事件
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoQueryOpenEvent extends Event<ShellQuery> {

    /**
     * 数据库节点
     */
    private ShellMongoDatabaseTreeItem dbItem;

    /**
     * 获取查询id
     *
     * @return 查询id
     */
    public String queryId() {
        return this.data().getUid();
    }

    public ShellMongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }
}
