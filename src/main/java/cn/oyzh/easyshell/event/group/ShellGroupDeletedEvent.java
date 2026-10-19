package cn.oyzh.easyshell.event.group;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * 分组已删除事件
 *
 * @author oyzh
 * @since 2025-03-03
 */
public class ShellGroupDeletedEvent extends Event<String> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s deleted] ", I18nHelper.folder(), this.data());
    }
}
