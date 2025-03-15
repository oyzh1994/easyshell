package cn.oyzh.easyshell.event.group;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ShellGroupAddedEvent extends Event<String> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s added] ", I18nHelper.group(), this.data());
    }
}
