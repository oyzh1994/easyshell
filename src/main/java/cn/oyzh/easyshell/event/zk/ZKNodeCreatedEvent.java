package cn.oyzh.easyshell.event.zk;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ZKNodeCreatedEvent extends Event<String> implements EventFormatter {

    private ShellZKClient client;

    public ShellZKClient getClient() {
        return client;
    }

    public void setClient(ShellZKClient client) {
        this.client = client;
    }

    public ShellConnect connect(){
        return this.client.zkConnect();
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s created, path:%s] ", I18nHelper.connect(), this.connect().getName(), this.data());
    }
}
