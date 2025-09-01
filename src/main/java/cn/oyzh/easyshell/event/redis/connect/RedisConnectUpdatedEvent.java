package cn.oyzh.easyshell.event.redis.connect;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/12/08
 */
public class RedisConnectUpdatedEvent extends Event<ShellConnect> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s] "+ I18nHelper.connectionUpdated(), this.data().getName());
    }
}
