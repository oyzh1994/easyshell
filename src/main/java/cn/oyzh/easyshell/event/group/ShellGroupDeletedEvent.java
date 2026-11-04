package cn.oyzh.easyshell.event.group;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.easyshell.util.ShellI18nHelper;

/**
 * 分组已删除事件
 *
 * @author oyzh
 * @since 2025-03-03
 */
public class ShellGroupDeletedEvent extends Event<String> implements EventFormatter {

    @Override
    public String eventFormat() {
return ShellI18nHelper.eventGroupDeleted(this.data());
    }
}
