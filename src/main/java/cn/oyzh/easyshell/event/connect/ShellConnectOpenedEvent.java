package cn.oyzh.easyshell.event.connect;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ShellConnectOpenedEvent extends Event<ShellConnectTreeItem>  {

//    public SSHClient client() {
//        return this.data().client();
//    }

    public ShellConnect connect() {
        return this.data().value();
    }
}
