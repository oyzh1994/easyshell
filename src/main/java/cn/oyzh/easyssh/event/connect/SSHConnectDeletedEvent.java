package cn.oyzh.easyssh.event.connect;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class SSHConnectDeletedEvent extends Event<SSHConnect> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s deleted] ", I18nHelper.connect(), this.data().getName());
    }
}
