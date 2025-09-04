package cn.oyzh.easyshell.event.zk.connection;

import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ZKConnectionClosedEvent extends Event<ZKClient> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s closed] ", I18nHelper.connect(), this.data().connectName());
    }

    public ZKConnect connect() {
        return this.data().zkConnect();
    }
}
