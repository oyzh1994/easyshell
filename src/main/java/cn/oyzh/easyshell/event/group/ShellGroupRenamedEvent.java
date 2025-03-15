package cn.oyzh.easyshell.event.group;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ShellGroupRenamedEvent extends Event<String> implements EventFormatter {

    @Setter
    @Accessors(chain = false, fluent = true)
    private String oldName;

    @Override
    public String eventFormat() {
        return String.format("[%s:%s renamed from %s] ", I18nHelper.group(), this.data(), this.oldName);
    }
}
