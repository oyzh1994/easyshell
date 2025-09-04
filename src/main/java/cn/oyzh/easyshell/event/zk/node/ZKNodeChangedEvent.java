package cn.oyzh.easyshell.event.zk.node;

import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ZKNodeChangedEvent extends Event<String> implements EventFormatter {

    private ZKClient client;

    public ZKClient getClient() {
        return client;
    }

    public void setClient(ZKClient client) {
        this.client = client;
    }

    public ZKConnect connect() {
        return this.client.zkConnect();
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s changed, path:%s] ", I18nHelper.connect(), this.connect().getName(), this.data());
    }

}
