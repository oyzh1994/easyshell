//package cn.oyzh.easyshell.ftp;
//
//import cn.oyzh.common.log.JulLog;
//import cn.oyzh.easyshell.domain.ShellConnect;
//import cn.oyzh.easyshell.exception.ShellException;
//import org.apache.commons.net.ftp.FTPSClient;
//import org.apache.commons.net.util.TrustManagerUtils;
//
//import java.io.IOException;
//
///**
// * ftps客户端
// *
// * @author oyzh
// * @since 2025/05/23
// */
//public class ShellFTPSClient extends ShellBaseFTPClient {
//
//    public static void main(String[] args) throws IOException {
//
//        ShellConnect connect = new ShellConnect();
//        connect.setHost("192.168.3.4:21");
//        connect.setUser("ssh");
//        connect.setPassword("123456");
//
//        ShellFTPSClient ftpsClient = new ShellFTPSClient(connect);
//        ftpsClient.start();
//        ftpsClient.close();
//    }
//
//    public ShellFTPSClient(ShellConnect shellConnect) {
//        super(shellConnect);
//        FTPSClient ftpsClient = new FTPSClient();
//        ftpsClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
//        super.setFtpClient(ftpsClient);
//    }
//
//    @Override
//    public FTPSClient getFtpClient() {
//        return (FTPSClient) super.getFtpClient();
//    }
//
//    @Override
//    public void start(int timeout) {
//        if (this.isConnected()) {
//            return;
//        }
//        try {
//            this.initClient();
//            // 连接信息
//            int port = this.shellConnect.hostPort();
//            String hostIp = this.shellConnect.hostIp();
//            // 开始连接时间
//            long starTime = System.currentTimeMillis();
//            FTPSClient ftpsClient = this.getFtpClient();
//            ftpsClient.setConnectTimeout(3000);
//            ftpsClient.setDataTimeout(3000);
//            ftpsClient.connect(hostIp, port);
//            if (!this.isConnected()) {
//                JulLog.warn("shellFTPSClient connect fail.");
//                return;
//            }
//            if (!ftpsClient.login(this.shellConnect.getUser(), this.shellConnect.getPassword())) {
//                JulLog.warn("shellFTPSClient login fail.");
//                return;
//            }
//            // 启用 TLS 加密
//            this.getFtpClient().execPBSZ(0);
//            this.getFtpClient().execPROT("P");
//            // 启用被动模式
//            this.getFtpClient().enterLocalPassiveMode();
//            this.lsFile("/");
//            long endTime = System.currentTimeMillis();
//            JulLog.info("shellFTPSClient connected used:{}ms.", (endTime - starTime));
//        } catch (Exception ex) {
//            JulLog.warn("shellFTPSClient start error", ex);
//            throw new ShellException(ex);
//        }
//    }
//}
