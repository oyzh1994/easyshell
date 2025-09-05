//package cn.oyzh.easyshell.event.zk;
//
//import cn.oyzh.easyshell.domain.ShellConnect;
//import cn.oyzh.easyshell.zk.ShellZKClient;
//import cn.oyzh.event.Event;
//
///**
// * @author oyzh
// * @since 2023/9/18
// */
//public class ShellZKNodeRemovedEvent extends Event<String>   {
//
//    public ShellZKClient getClient() {
//        return client;
//    }
//
//    public void setClient(ShellZKClient client) {
//        this.client = client;
//    }
//
//    private ShellZKClient client;
//
//    public ShellConnect connect(){
//        return this.client.zkConnect();
//    }
//}
