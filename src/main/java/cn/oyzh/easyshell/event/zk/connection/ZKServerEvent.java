package cn.oyzh.easyshell.event.zk.connection;

import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024/4/1
 */
public class ZKServerEvent extends Event<ZKClient> {

    public ZKConnect zkConnect() {
        return this.data().zkConnect();
    }
}
