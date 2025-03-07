package cn.oyzh.easyssh.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.sftp.SSHSftp;
import cn.oyzh.easyssh.sftp.SSHSftpManager;
import cn.oyzh.easyssh.sftp.SftpAttr;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.sftp.download.SftpDownloadCanceled;
import cn.oyzh.easyssh.sftp.download.SftpDownloadChanged;
import cn.oyzh.easyssh.sftp.download.SftpDownloadEnded;
import cn.oyzh.easyssh.sftp.download.SftpDownloadFailed;
import cn.oyzh.easyssh.sftp.download.SftpDownloadManager;
import cn.oyzh.easyssh.sftp.upload.SftpUploadCanceled;
import cn.oyzh.easyssh.sftp.upload.SftpUploadChanged;
import cn.oyzh.easyssh.sftp.upload.SftpUploadEnded;
import cn.oyzh.easyssh.sftp.upload.SftpUploadFailed;
import cn.oyzh.easyssh.sftp.upload.SftpUploadManager;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * ssh终端
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class SSHClient {

    /**
     * JSch对象
     */
    private static final JSch JSCH = new JSch();

    /**
     * ssh信息
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private final SSHConnect sshConnect;

    /**
     * ssh会话
     */
    @Getter
    private Session session;

    /**
     * 连接状态监听器列表
     */
    private final List<ChangeListener<SSHConnState>> connStateListeners = new ArrayList<>();

    public SSHClient(@NonNull SSHConnect sshConnect) {
        this.sshConnect = sshConnect;
    }

    /**
     * 连接状态
     */
    @Getter
    private final ReadOnlyObjectWrapper<SSHConnState> state = new ReadOnlyObjectWrapper<>();

    /**
     * 获取连接状态
     *
     * @return 连接状态
     */
    public SSHConnState state() {
        return this.stateProperty().get();
    }

    /**
     * 连接状态属性
     *
     * @return 连接状态属性
     */
    public ReadOnlyObjectProperty<SSHConnState> stateProperty() {
        return this.state.getReadOnlyProperty();
    }

//    /**
//     * 添加连接状态监听器
//     *
//     * @param listener 监听器
//     */
//    public void addConnStateListener(@NonNull ChangeListener<SSHConnState> listener) {
//        if (!this.connStateListeners.contains(listener)) {
//            this.connStateListeners.add(listener);
//            this.stateProperty().addListener(listener);
//        }
//    }
//
//    /**
//     * 移除连接状态监听器
//     *
//     * @param listener 监听器
//     */
//    public void removeConnStateListener(ChangeListener<SSHConnState> listener) {
//        if (listener != null) {
//            this.connStateListeners.remove(listener);
//            this.stateProperty().removeListener(listener);
//        }
//    }

    /**
     * 初始化客户端
     */
    private void initClient() throws JSchException {
        if (JulLog.isInfoEnabled()) {
            JulLog.info("initClient user:{} password:{} host:{}", this.sshConnect.getUser(), this.sshConnect.getPassword(), this.sshConnect.getHost());
        }
        // 创建会话
        this.session = JSCH.getSession(this.sshConnect.getUser(), this.sshConnect.hostIp(), this.sshConnect.hostPort());
        // 主机密码
        if (StringUtil.isNotBlank(this.sshConnect.getPassword())) {
            this.session.setPassword(this.sshConnect.getPassword());
        }
        Properties config = new Properties();
        // 去掉首次连接确认
        config.put("StrictHostKeyChecking", "no");
        // 设置终端类型
//        config.put("TERM", "xterm-256color");
        config.put("term", "xterm-256color");
        config.put("COLORTERM", "truecolor");
        this.session.setConfig(config);
//        session.setConfig("term", "xterm-256color");
        // 超时连接时间为3秒
        this.session.setTimeout(this.sshConnect.connectTimeOutMs());
    }

    /**
     * 关闭客户端
     */
    public void close() {
        try {
            this.sftpManager.close();
            if (this.shell != null) {
                this.shell.close();
            }
            if (this.session != null) {
                this.session.disconnect();
                this.state.set(SSHConnState.CLOSED);
            }
            this.shell = null;
            this.session = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 重置客户端
     */
    public void reset() {
        // 移除监听器
        if (!this.connStateListeners.isEmpty()) {
            for (ChangeListener<SSHConnState> listener : connStateListeners) {
                this.stateProperty().removeListener(listener);
            }
            this.connStateListeners.clear();
        }
        this.close();
        this.state.set(SSHConnState.NOT_INITIALIZED);
    }

    /**
     * 开始连接客户端
     */
    public void start() throws Exception {
        this.start(this.connectTimeout());
    }

    /**
     * 开始连接客户端
     *
     * @param timeout 超时时间
     */
    public void start(int timeout) throws Exception {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        // 初始化客户端
        this.initClient();
        try {
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            // 初始化连接池
            this.state.set(SSHConnState.CONNECTING);
            // 执行连接
            this.session.connect(timeout);
            // 判断连接结果
            if (this.session.isConnected()) {
                this.state.set(SSHConnState.CONNECTED);
            } else if (this.state.get() == SSHConnState.FAILED) {
                this.state.set(null);
            } else {
                this.state.set(SSHConnState.FAILED);
            }
            long endTime = System.currentTimeMillis();
            JulLog.info("sshClient connected used:{}ms.", (endTime - starTime));
        } catch (Exception ex) {
            this.state.set(SSHConnState.FAILED);
            throw ex;
        }
    }

    /**
     * 是否连接中
     *
     * @return 结果
     */
    public boolean isConnecting() {
        if (!this.isClosed()) {
            return this.state.get() == SSHConnState.CONNECTING;
        }
        this.state.set(SSHConnState.CLOSED);
        return false;
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public boolean isConnected() {
        if (!this.isClosed()) {
            return this.state.get().isConnected();
        }
        this.state.set(SSHConnState.CLOSED);
        return false;
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    public boolean isClosed() {
        return this.session == null || !this.session.isConnected() || !this.state.get().isConnected();
    }

//    /**
//     * 当前连接名称
//     *
//     * @return 名称
//     */
//    public String infoName() {
//        return this.sshConnect.getName();
//    }

//    /**
//     * 获取交互式终端
//     *
//     * @return 交互式终端
//     */
//    public SSHShell shell() throws Exception {
//        if (!this.isConnected()) {
//            return null;
//        }
//        try {
//            // 执行连接
//            if (this.shell == null) {
//                ChannelShell shell = (ChannelShell) this.session.openChannel("shell");
//                this.shell = new SSHShell(shell);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw ex;
//        }
//        return this.shell;
//    }

    public Object connectName() {
        return this.sshConnect.getName();
    }

//    public void bindStream(PtyProcess process) throws Exception {
//        if (session == null) {
//            this.start();
//        }
//        // 打开一个通道
//        Channel channel = session.openChannel("shell");
////        channel.setInputStream(System.in);
////        channel.setOutputStream(System.out);
//
//        InputStream inputStream = process.getInputStream();
//        OutputStream outputStream = process.getOutputStream();
//
//        inputStream.transferTo(channel.getOutputStream());
//        channel.getInputStream().transferTo(outputStream);
//
//        // 读取 SSH 通道的输出并写入 pty4j 进程的输入
//        Thread readThread = new Thread(() -> {
//            try {
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while (true) {
//                    bytesRead = channel.getInputStream().read(buffer);
//                    if (bytesRead != -1) {
//                        outputStream.write(buffer, 0, bytesRead);
//                        outputStream.flush();
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        readThread.start();
//
//        // 读取 pty4j 进程的输出并写入 SSH 通道的输入
//        Thread writeThread = new Thread(() -> {
//            try {
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while (true) {
//                    bytesRead = inputStream.read(buffer);
//                    if (bytesRead != -1) {
//                        channel.getOutputStream().write(buffer, 0, bytesRead);
//                        channel.getOutputStream().flush();
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        writeThread.start();
//        channel.connect();
//    }
//
//    public void bindStream1(TtyConnector connector) throws Exception {
//        if (session == null) {
//            this.start();
//        }
//        // 打开一个通道
//        Channel channel = session.openChannel("shell");
    /// /        channel.setInputStream(System.in);
    /// /        channel.setOutputStream(System.out);
//
//        // 读取 SSH 通道的输出并写入 pty4j 进程的输入
//        Thread readThread = new Thread(() -> {
//            try {
//                char[] buffer = new char[1024];
//                int bytesRead;
//                while (true) {
//                    bytesRead = connector.read(buffer, 0, buffer.length);
//                    if (bytesRead > 0) {
//                        String s = new String(buffer, 0, bytesRead);
//                        channel.getOutputStream().write(s.getBytes());
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        readThread.start();
//
//        // 读取 pty4j 进程的输出并写入 SSH 通道的输入
//        Thread writeThread = new Thread(() -> {
//            try {
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while (true) {
//                    bytesRead = channel.getInputStream().read(buffer, 0, buffer.length);
//                    if (bytesRead > 0) {
//                        connector.write(buffer);
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        writeThread.start();
//        channel.connect();
//    }

    @Getter
    private SSHShell shell;

    public SSHShell openShell() {
        if (this.shell == null || this.shell.isClosed()) {
            try {
                ChannelShell channel = (ChannelShell) this.session.openChannel("shell");
                channel.setInputStream(System.in);
                channel.setOutputStream(System.out);
                channel.setPtyType("xterm");
                this.shell = new SSHShell(channel);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.shell;
    }

    private final SSHSftpManager sftpManager = new SSHSftpManager();

    private final SftpUploadManager sftpUploadManager = new SftpUploadManager();

    private final SftpDownloadManager sftpDownloadManager = new SftpDownloadManager();

    public SSHSftp openSftp() {
        if (!this.sftpManager.hasAvailable()) {
            try {
                ChannelSftp channel = (ChannelSftp) this.session.openChannel("sftp");
                SSHSftp sftp = new SSHSftp(channel);
//                SSHSftp sftp = new SSHSftp(channel, this.sftpUploadManager);
                sftp.connect(this.connectTimeout());
                this.sftpManager.push(sftp);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this.sftpManager.take();
    }

    public String exec(String command) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) this.session.openChannel("exec");
            channel.setCommand(command);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            channel.setOutputStream(stream);
            channel.connect();
            while (channel.isConnected()) {
                Thread.sleep(5);
            }
            return stream.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        return null;
    }

    public String exec_id_un(int uid) {
        return this.exec("/usr/bin/id -un " + uid);
    }

    public String exec_id_gn(int gid) {
        return this.exec("/usr/bin/id -gn " + gid);
    }

    public int connectTimeout() {
        return this.sshConnect.connectTimeOutMs();
    }

    private SftpAttr attr;

    public SftpAttr getAttr() {
        if (this.attr == null) {
            this.attr = new SftpAttr();
        }
        return this.attr;
    }

    public void upload(File localFile, String remoteFile) throws SftpException {
        this.sftpUploadManager.createMonitor(localFile, remoteFile, this.openSftp());
    }

    public void cancelUpload() {
        this.sftpUploadManager.cancel();
    }

    public void setUploadEndedCallback(Consumer<SftpUploadEnded> callback) {
        this.sftpUploadManager.setUploadEndedCallback(callback);
    }

    public void setUploadFailedCallback(Consumer<SftpUploadFailed> callback) {
        this.sftpUploadManager.setUploadFailedCallback(callback);
    }

    public void setUploadCanceledCallback(Consumer<SftpUploadCanceled> callback) {
        this.sftpUploadManager.setUploadCanceledCallback(callback);
    }

    public void setUploadChangedCallback(Consumer<SftpUploadChanged> callback) {
        this.sftpUploadManager.setUploadChangedCallback(callback);
    }

    public void download(File localFile, SftpFile remoteFile) throws SftpException {
        this.sftpDownloadManager.createMonitor(localFile, remoteFile, this.openSftp());
    }

    public void cancelDownload() {
        this.sftpDownloadManager.cancel();
    }

    public void setDownloadEndedCallback(Consumer<SftpDownloadEnded> callback) {
        this.sftpDownloadManager.setDownloadEndedCallback(callback);
    }

    public void setDownloadFailedCallback(Consumer<SftpDownloadFailed> callback) {
        this.sftpDownloadManager.setDownloadFailedCallback(callback);
    }

    public void setDownloadCanceledCallback(Consumer<SftpDownloadCanceled> callback) {
        this.sftpDownloadManager.setDownloadCanceledCallback(callback);
    }

    public void setDownloadChangedCallback(Consumer<SftpDownloadChanged> callback) {
        this.sftpDownloadManager.setDownloadChangedCallback(callback);
    }
}
