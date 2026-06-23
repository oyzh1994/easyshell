package cn.oyzh.easyshell.event.mysql.query;

import cn.oyzh.easyshell.trees.mysql.query.ShellMysqlQueryTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMysqlQueryDeletedEvent extends Event<ShellMysqlQueryTreeItem> implements EventFormatter {

    public String queryId() {
        return this.data().value().getUid();
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] deleted", I18nHelper.query(), this.data().queryName());
    }
}
