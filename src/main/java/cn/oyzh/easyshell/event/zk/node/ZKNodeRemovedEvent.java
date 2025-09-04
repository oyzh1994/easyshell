package cn.oyzh.easyshell.event.zk.node;

import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ZKNodeRemovedEvent extends Event<String>   {

    public ZKClient getClient() {
        return client;
    }

    public void setClient(ZKClient client) {
        this.client = client;
    }

    private ZKClient client;

    public ZKConnect connect(){
        return this.client.zkConnect();
    }
}
