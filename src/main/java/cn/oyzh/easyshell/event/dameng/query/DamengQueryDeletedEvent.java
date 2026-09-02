package cn.oyzh.easyshell.event.dameng.query;

import cn.oyzh.easyshell.trees.dameng.query.DamengQueryTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class DamengQueryDeletedEvent extends Event<DamengQueryTreeItem> {

    public String queryId() {
        return this.data().value().getUid();
    }
}
