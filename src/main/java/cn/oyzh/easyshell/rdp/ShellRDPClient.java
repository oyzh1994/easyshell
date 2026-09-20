package cn.oyzh.easyshell.rdp;


import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.network.NetworkUtil;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.ProcessBuilderUtil;
import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.fx.rdp.RdpView;
import com.tangluobo.rdp4j.RdpClient;
import com.tangluobo.rdp4j.frontend.FxRdpFrontend;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author oyzh
 * @since 2025-09-12
 */
public class ShellRDPClient implements ShellBaseClient {

    /**
     * 连接
     */
    private final ShellConnect shellConnect;

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>(ShellConnState.NOT_INITIALIZED);

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellBaseClient.super.onStateChanged(state3);

    public ShellRDPClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    /**
     * rdp客户端
     */
    private RdpClient client;

    /**
     * rdp前端
     */
    private FxRdpFrontend frontend;

    /**
     * 初始化客户端
     */
    protected void initClient() throws IOException {
        if (this.client == null) {
            this.frontend = new FxRdpFrontend();
            this.client = new RdpClient(this.frontend);
            this.client.setOnDisconnected((s, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                JulLog.warn("RdpClient disconnected reason:{}", s, throwable);
            });
        }
    }

    @Override
    public void start(int timeout) throws Throwable {
        if (ShellRDPUtil.isBuiltIn(this.shellConnect)) {
            try {
                // 初始化客户端
                this.initClient();
                // 开始连接时间
                final AtomicLong starTime = new AtomicLong();
                // 开始连接时间
                starTime.set(System.currentTimeMillis());
                // 更新连接状态
                this.state.set(ShellConnState.CONNECTING);
                // 异步连接
                AtomicReference<Exception> ref = new AtomicReference<>();
                DownLatch latch = DownLatch.of();
                ThreadUtil.startVirtual(() -> {
                    try {
                        String host = this.shellConnect.hostIp();
                        int port = this.shellConnect.hostPort();
                        String user = this.shellConnect.getUser();
                        String password = this.shellConnect.getPassword();
                        String domain = this.shellConnect.getDomain();
                        if (StringUtil.isBlank(domain)) {
                            domain = null;
                        }
                        String resolution = this.shellConnect.getResolution();
                        int width = 1920;
                        int height = 1080;
                        if (StringUtil.isNotBlank(resolution)) {
                            width = Integer.parseInt(resolution.split("x")[0].trim());
                            height = Integer.parseInt(resolution.split("x")[1].trim());
                        }
                        Integer color = this.shellConnect.getExtra("color");
                        if (color == null) {
                            color = 32;
                        }
                        boolean sslMode = this.shellConnect.isSSLMode();
                        Boolean remoteAudio = this.shellConnect.getExtra("remoteAudio");
                        if (remoteAudio == null) {
                            remoteAudio = false;
                        }
                        Boolean redirectClipboard = this.shellConnect.getExtra("redirectClipboard");
                        if (redirectClipboard == null) {
                            redirectClipboard = false;
                        }
                        this.client.connect(host, port, user, password, domain, width, height, color, sslMode, remoteAudio, redirectClipboard);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        ref.set(ex);
                    } finally {
                        latch.countDown();
                    }
                });
                // 连接成功前阻塞线程
                latch.await(timeout);
                // 抛出异常
                if (ref.get() != null) {
                    throw ref.get();
                }
                if (this.isConnected()) {
                    // 更新连接状态
                    this.state.set(ShellConnState.CONNECTED);
                    // 添加到状态监听器队列
                    ShellClientChecker.push(this);
                } else {// 连接未成功则关闭
                    this.close();
                    if (this.state.get() == ShellConnState.FAILED) {
                        this.state.set(null);
                    } else {
                        this.state.set(ShellConnState.FAILED);
                    }
                }
            } catch (Throwable ex) {
                this.state.set(ShellConnState.FAILED);
                if (ex.getCause() != null) {
                    ex = ex.getCause();
                }
                JulLog.warn("Mysql client start error", ex);
                throw new ShellException(ex);
            }
        } else {
            File rdpFile = ShellRDPUtil.initRDPFile(this.shellConnect);
            // 执行命令
            if (OSUtil.isMacOS()) {
                ProcessBuilderUtil.exec("open", rdpFile.getPath());
            } else if (OSUtil.isWindows()) {
                ProcessBuilderUtil.exec("mstsc", rdpFile.getPath());
            }
        }
    }

    /**
     * 初始化rdp组件
     *
     * @param rdpView rdp组件
     */
    public void initRdpView(RdpView rdpView) throws IOException {
        if (this.client == null) {
            this.initClient();
        }
        rdpView.steup(this.client, this.frontend);
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    @Override
    public boolean isConnected() {
        if (this.client != null) {
            return this.client.isConnected();
        }
        String ip = this.getShellConnect().hostIp();
        int port = this.getShellConnect().hostPort();
        return NetworkUtil.reachable(ip, port, 1000);
    }

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    @Override
    public void close() throws Exception {
        try {
            if (this.client != null) {
                this.client.disconnect();
            }
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
            this.stateProperty().unbind();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
