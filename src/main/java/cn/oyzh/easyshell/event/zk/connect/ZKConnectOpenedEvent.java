// package cn.oyzh.easyshell.event.zk.connect;
//
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyzk.trees.connect.ZKConnectTreeItem;
// import cn.oyzh.easyshell.zk.ShellZKClient;
// import cn.oyzh.event.Event;
//
// /**
//  * @author oyzh
//  * @since 2023/9/18
//  */
// public class ZKConnectOpenedEvent extends Event<ZKConnectTreeItem>  {
//
//     public ShellZKClient client() {
//         return this.data().getClient();
//     }
//
//     public ShellConnect connect() {
//         return this.data().getClient().zkConnect();
//     }
// }
