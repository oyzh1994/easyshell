package cn.oyzh.easyshell.event.mongo.function;

import cn.oyzh.easyshell.mongo.trees.database.MongoDatabaseTreeItem;
import cn.oyzh.easyshell.mongo.trees.function.ShellMongoFunctionTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/01/30
 */
public class ShellMongoFunctionDroppedEvent extends Event<ShellMongoFunctionTreeItem> implements EventFormatter {

    public String functionName() {
        return this.data().functionName();
    }

    public MongoDatabaseTreeItem getDbItem() {
        return this.data().dbItem();
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] dropped", I18nHelper.function(), this.functionName());
    }
}
