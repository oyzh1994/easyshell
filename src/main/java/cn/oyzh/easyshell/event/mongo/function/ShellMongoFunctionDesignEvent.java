package cn.oyzh.easyshell.event.mongo.function;

import cn.oyzh.easyshell.mongo.function.MongoFunction;
import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellMongoFunctionDesignEvent extends Event<MongoFunction> {

    private MongoDatabaseTreeItem dbItem;

    public String functionName() {
        return this.data().getName();
    }

    public MongoDatabaseTreeItem getDbItem() {
        return dbItem;
    }

    public void setDbItem(MongoDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
    }

}
