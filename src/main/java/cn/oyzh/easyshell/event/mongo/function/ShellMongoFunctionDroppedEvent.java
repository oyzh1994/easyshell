package cn.oyzh.easyshell.event.mongo.function;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.function.ShellMongoFunctionTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * mongodb函数已删除事件
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoFunctionDroppedEvent extends Event<ShellMongoFunctionTreeItem> implements EventFormatter {

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.data().functionName();
    }

    /**
     * 获取数据库节点
     *
     * @return 数据库节点
     */
    public ShellMongoDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] dropped", I18nHelper.function(), this.functionName());
    }
}
