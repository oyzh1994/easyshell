package cn.oyzh.easyshell.event.dameng.query;

import cn.oyzh.easyshell.trees.dameng.query.ShellDamengQueryTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellDamengQueryDeletedEvent extends Event<ShellDamengQueryTreeItem> {

    public String queryId() {
        return this.data().value().getUid();
    }
}
