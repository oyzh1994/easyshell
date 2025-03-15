package cn.oyzh.easyssh.event.connect;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.trees.connect.SSHConnectTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class SSHConnectOpenedEvent extends Event<SSHConnectTreeItem>  {

//    public SSHClient client() {
//        return this.data().client();
//    }

    public SSHConnect connect() {
        return this.data().value();
    }
}
