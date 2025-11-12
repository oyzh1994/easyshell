package cn.oyzh.easyshell.event.mysql.query;

import cn.oyzh.easyshell.trees.mysql.query.ShellMysqlQueryTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMysqlQueryDeletedEvent extends Event<ShellMysqlQueryTreeItem> {

    public String queryId() {
        return this.data().value().getUid();
    }
}
