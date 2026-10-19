package cn.oyzh.easyshell.event.mongo.function;

import cn.oyzh.easyshell.mongo.function.MongoFunction;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.Event;

/**
 * mongodb函数设计事件
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoFunctionDesignEvent extends Event<MongoFunction> {

    /**
     * 数据库节点
     */
    private ShellMongoDatabaseTreeItem dbItem;

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
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
