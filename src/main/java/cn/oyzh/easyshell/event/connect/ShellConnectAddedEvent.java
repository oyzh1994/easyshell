package cn.oyzh.easyshell.event.connect;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * 连接已新增事件
 *
 * @author oyzh
 * @since 2025-03-03
 */
public class ShellConnectAddedEvent extends Event<ShellConnect> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] added", I18nHelper.connect(), this.data().getName());
    }
}
