// package cn.oyzh.easyshell.event.zk.query;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.domain.zk.ZKQuery;
// import cn.oyzh.easyshell.zk.ShellZKClient;
// import cn.oyzh.event.Event;
//
// /**
//  * @author oyzh
//  * @since 2024-11-18
//  */
// public class ZKOpenQueryEvent extends Event<ZKQuery> {
//     public ShellZKClient getClient() {
//         return client;
//     }
//
//     public void setClient(ShellZKClient client) {
//         this.client = client;
//     }
//
//     private ShellZKClient client;
//
//     public ShellConnect zkConnect() {
//         return this.client.zkConnect();
//     }
// }
