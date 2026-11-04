package cn.oyzh.easyshell.event.key;

import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.easyshell.util.ShellI18nHelper;

/**
 * 密钥已新增事件
 *
 * @author oyzh
 * @since 2025-04-03
 */
public class ShellKeyAddedEvent extends Event<ShellKey> implements EventFormatter {

    @Override
    public String eventFormat() {
return ShellI18nHelper.eventKeyAdded(this.data().getName());
    }
}
