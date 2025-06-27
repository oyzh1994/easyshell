//package cn.oyzh.easyshell.sshj.sftp;
//
//import net.schmizz.sshj.sftp.SFTPClient;
//
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
///**
// * sftp通道管理器
// *
// * @author oyzh
// * @since 2025-06-07
// */
//public class ShellSFTPClintPool implements AutoCloseable {
//
//    /**
//     * 初始通道数量
//     */
//    private final int initSize;
//
//    /**
//     * 客户端
//     */
//    private ShellSFTPClient client;
//
//    /**
//     * 通道列表
//     */
//    private List<SFTPClient> clients = new CopyOnWriteArrayList<>();
//
//    public ShellSFTPClintPool(ShellSFTPClient client) {
//        this(client, 3);
//    }
//
//    public ShellSFTPClintPool(ShellSFTPClient client, int initSize) {
//        this.client = client;
//        this.initSize = initSize;
//    }
//
//    /**
//     * 初始化
//     */
//    private void init() {
//        while (this.clients.size() < this.initSize) {
//            try {
//                SFTPClient channel = this.client.newSFTPClient();
//                if (channel == null) {
//                    break;
//                }
//                this.clients.add(channel);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 借用通道
//     *
//     * @return 通道
//     */
//    public SFTPClient borrowChannel() {
//        try {
//            if (this.clients.isEmpty()) {
//                this.init();
//            }
//            return this.clients.removeFirst();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 归还通道
//     *
//     * @param client 通道
//     */
//    public void returnChannel(SFTPClient client) {
//        if (client == null) {
//            return;
//        }
//        this.clients.add(client);
//    }
//
//    @Override
//    public void close() throws Exception {
//        for (SFTPClient client : this.clients) {
//            client.close();
//        }
//        this.clients.clear();
//        this.clients = null;
//        this.client = null;
//    }
//}
