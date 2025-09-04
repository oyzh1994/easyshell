package cn.oyzh.easyshell.event.zk.query;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.zk.ZKQuery;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024-11-18
 */
public class ZKOpenQueryEvent extends Event<ZKQuery> {
    public ZKClient getClient() {
        return client;
    }

    public void setClient(ZKClient client) {
        this.client = client;
    }

    private ZKClient client;

    public ShellConnect zkConnect() {
        return this.client.zkConnect();
    }
}
