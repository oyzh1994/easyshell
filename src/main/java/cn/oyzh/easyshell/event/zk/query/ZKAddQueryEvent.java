package cn.oyzh.easyshell.event.zk.query;

import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2024-11-18
 */
public class ZKAddQueryEvent extends Event<ZKClient> {

    public ZKConnect zkConnect(){
        return this.data().zkConnect();
    }
}
