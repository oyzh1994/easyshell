package cn.oyzh.easyshell.event.dameng.query;

import cn.oyzh.easyshell.trees.dameng.query.ShellDamengQueryTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellDamengQueryDeletedEvent extends Event<ShellDamengQueryTreeItem> implements EventFormatter {

    public String queryId() {
        return this.data().value().getUid();
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] deleted", I18nHelper.query(), this.data().queryName());
    }
}
