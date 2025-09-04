package cn.oyzh.easyshell.event.zk.history;

import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.easyshell.trees.zk.ZKNodeTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/23
 */
public class ZKHistoryRestoreEvent extends Event<byte[]> implements EventFormatter {

    private ZKNodeTreeItem item;

    public void setItem(ZKNodeTreeItem item) {
        this.item = item;
    }

    public ZKNodeTreeItem getItem() {
        return item;
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s path:%s restored data] ", I18nHelper.connect(), this.connect().getName(), this.item.nodePath());
    }

    public ZKConnect connect() {
        return this.item.zkConnect();
    }
}
