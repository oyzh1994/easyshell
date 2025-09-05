package cn.oyzh.easyshell.event.zk.history;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.zk.ShellZKNodeTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/23
 */
public class ZKHistoryRestoreEvent extends Event<byte[]> implements EventFormatter {

    private ShellZKNodeTreeItem item;

    public void setItem(ShellZKNodeTreeItem item) {
        this.item = item;
    }

    public ShellZKNodeTreeItem getItem() {
        return item;
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s path:%s restored data] ", I18nHelper.connect(), this.connect().getName(), this.item.nodePath());
    }

    public ShellConnect connect() {
        return this.item.zkConnect();
    }
}
