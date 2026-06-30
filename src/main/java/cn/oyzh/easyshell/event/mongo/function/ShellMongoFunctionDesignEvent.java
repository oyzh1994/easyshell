package cn.oyzh.easyshell.event.mongo.function;

import cn.oyzh.easyshell.mongo.function.MongoFunction;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellMongoFunctionDesignEvent extends Event<MongoFunction> {

    private ShellMongoDatabaseTreeItem dbItem;

    public String functionName() {
        return this.data().getName();
    }

    public ShellMongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(ShellMongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
