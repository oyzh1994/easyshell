// package cn.oyzh.easyshell.sshj;
//
//
// import cn.oyzh.easyshell.sftp.ShellSFTPChannel;
//
// import java.util.List;
// import java.util.concurrent.CopyOnWriteArrayList;
//
// /**
//  * sftp通道管理器
//  *
//  * @author oyzh
//  * @since 2025-06-07
//  */
// public class ShellSFTPChannelPool implements AutoCloseable {
//
//     /**
//      * 初始通道数量
//      */
//     private final int initSize;
//
//     /**
//      * 客户端
//      */
//     private ShellSFTPClient client;
//
//     /**
//      * 通道列表
//      */
//     private List<ShellSFTPChannel> channels = new CopyOnWriteArrayList<>();
//
//     public ShellSFTPChannelPool(ShellSFTPClient client) {
//         this(client, 3);
//     }
//
//     public ShellSFTPChannelPool(ShellSFTPClient client, int initSize) {
//         this.client = client;
//         this.initSize = initSize;
//     }
//
//     /**
//      * 初始化
//      */
//     private void init() {
//         if (this.channels.size() < this.initSize) {
//             this.channels.add(this.client.newChannel());
//         }
//     }
//
//     /**
//      * 借用通道
//      *
//      * @return 通道
//      */
//     public ShellSFTPChannel borrowChannel() {
//         try {
//             if (this.channels.isEmpty()) {
//                 this.init();
//             }
//             return this.channels.removeFirst();
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return null;
//     }
//
//     /**
//      * 归还通道
//      *
//      * @param channel 通道
//      */
//     public void returnChannel(ShellSFTPChannel channel) {
//         if (channel == null || channel.isClosed()) {
//             return;
//         }
//         this.channels.add(channel);
//     }
//
//     @Override
//     public void close() throws Exception {
//         for (ShellSFTPChannel channel : this.channels) {
//             channel.close();
//         }
//         this.channels.clear();
//         this.channels = null;
//         this.client = null;
//     }
// }
