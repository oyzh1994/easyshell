package cn.oyzh.easyshell.event.mariadb.query;

import cn.oyzh.easyshell.trees.mariadb.query.ShellMariadbQueryTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * MariaDB查询已删除事件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQueryDeletedEvent extends Event<ShellMariadbQueryTreeItem> implements EventFormatter {

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
        return String.format("[%s:%s] deleted", I18nHelper.query(), this.data().queryName());
    }
}
