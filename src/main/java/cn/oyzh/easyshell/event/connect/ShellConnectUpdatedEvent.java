package cn.oyzh.easyshell.event.connect;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ShellConnectUpdatedEvent extends Event<ShellConnect> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s updated] ", I18nHelper.connect(), this.data().getName());
    }
}
