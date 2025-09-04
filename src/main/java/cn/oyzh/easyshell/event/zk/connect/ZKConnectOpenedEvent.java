// package cn.oyzh.easyshell.event.zk.connect;
//
// import cn.oyzh.easyshell.domain.zk.ZKConnect;
// import cn.oyzh.easyzk.trees.connect.ZKConnectTreeItem;
// import cn.oyzh.easyshell.zk.ZKClient;
// import cn.oyzh.event.Event;
//
// /**
//  * @author oyzh
//  * @since 2023/9/18
//  */
// public class ZKConnectOpenedEvent extends Event<ZKConnectTreeItem>  {
//
//     public ZKClient client() {
//         return this.data().getClient();
//     }
//
//     public ZKConnect connect() {
//         return this.data().getClient().zkConnect();
//     }
// }
