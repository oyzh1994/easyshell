package cn.oyzh.easyshell.event.connect;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.easyshell.util.ShellI18nHelper;

/**
 * 连接已删除事件
 *
 * @author oyzh
 * @since 2025-03-03
 */
public class ShellConnectDeletedEvent extends Event<ShellConnect> implements EventFormatter {

    @Override
    public String eventFormat() {
return ShellI18nHelper.eventConnectDeleted(this.data().getName());
    }
}
