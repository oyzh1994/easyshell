package cn.oyzh.easyshell.event.mongo.query;

import cn.oyzh.easyshell.trees.mongo.query.ShellMongoQueryTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.easyshell.util.ShellI18nHelper;

/**
 * mongodb查询已删除事件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMongoQueryDeletedEvent extends Event<ShellMongoQueryTreeItem>  implements EventFormatter {

    /**
     * 获取查询id
     *
     * @return 查询id
     */
    public String queryId() {
        return this.data().value().getUid();
    }

    @Override
    public String eventFormat() {
return ShellI18nHelper.eventQueryDeleted(this.data().queryName());
    }
}
