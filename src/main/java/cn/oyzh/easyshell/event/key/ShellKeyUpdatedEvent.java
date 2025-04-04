package cn.oyzh.easyshell.event.key;

import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025/04/04
 */
public class ShellKeyUpdatedEvent extends Event<ShellKey> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s] updated", I18nHelper.key1(), this.data().getName());
    }
}
