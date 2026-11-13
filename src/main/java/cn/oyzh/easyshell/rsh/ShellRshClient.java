package cn.oyzh.easyshell.rsh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.apache.commons.net.bsd.RCommandClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * rsh客户端
 *
 * @author oyzh
 * @since 2026-11-14
 */
public class ShellRshClient implements ShellBaseClient {

    /**
     * pty 握手探测超时，单位毫秒。
     */
    private static final int PTY_PROBE_TIMEOUT = 3000;

    /**
     * 客户端
     */
    private RCommandClient client;

    /**
     * 输入流，负责读取并剔除远端 pty 握手。
     */
    private RshPtyInputStream inputStream;

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

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    /**
     * 构造rsh客户端
     *
     * @param shellConnect 连接
     */
    public ShellRshClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    /**
     * 初始化客户端
     */
    private void initClient() {
        if (this.shellConnect.hostPort() >= 1024) {
            this.client = new PatchedRCommandClient();
        } else {
            this.client = new RCommandClient();
        }
        this.client.setCharset(ShellBaseClient.super.getCharset());
        // 代理处理
        if (this.shellConnect.isEnableProxy()) {
            this.client.setProxy(ShellProxyUtil.initProxy1(this.shellConnect.getProxyConfig()));
        }
    }

    /**
     * 构造交互式登录shell命令。
     *
     * @return shell命令
     */
    private String terminalCommand() {
        return RshPtySupport.buildCommand(this.shellConnect.getTermType());
    }

    @Override
    public void start(int timeout) throws Throwable {
        if (this.isConnected()) {
            return;
        }
        try {
            this.initClient();
            this.client.setConnectTimeout(timeout);
            this.state.set(ShellConnState.CONNECTING);
            int hostPort = this.shellConnect.hostPort();
            String hostIp = this.shellConnect.hostIp();
            DownLatch latch = DownLatch.of();
            AtomicReference<Throwable> ref = new AtomicReference<>();
            // 丢进线程执行，避免一直卡住
            ThreadUtil.start(() -> {
                try {
                    this.client.connect(hostIp, hostPort);
                } catch (Throwable e) {
                    ref.set(e);
                } finally {
                    latch.countDown();
                }
            });
            // 等待结束
            if (!latch.await(timeout)) {
                this.state.set(ShellConnState.FAILED);
                return;
            }
            // 异常
            if (ref.get() != null) {
                throw ref.get();
            }
            if (this.client.isConnected()) {
                String user = this.shellConnect.getUser();
                this.client.rcommand(user, user, this.terminalCommand(), false);
                // 探测远端 pty 能力，同时保留探测期间读取到的数据
                this.inputStream = new RshPtyInputStream(this.client.getInputStream());
                int soTimeout = -1;
                try {
                    soTimeout = this.client.getSoTimeout();
                    this.client.setSoTimeout(PTY_PROBE_TIMEOUT);
                } catch (Exception ignored) {
                }
                try {
                    this.inputStream.probe();
                } finally {
                    if (soTimeout >= 0) {
                        try {
                            this.client.setSoTimeout(soTimeout);
                        } catch (Exception ignored) {
                        }
                    }
                }
                this.state.set(ShellConnState.CONNECTED);
                // 添加到状态监听器队列
                ShellClientChecker.push(this);
            } else {
                this.state.set(ShellConnState.FAILED);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            JulLog.warn("Rsh client start error", ex);
            this.state.set(ShellConnState.FAILED);
            throw ex;
        } finally {
            // 执行一次gc，快速回收内存
            SystemUtil.gc();
        }
    }

    @Override
    public void close() {
        try {
            this.inputStream = null;
            if (this.client != null) {
                this.client.disconnect();
                this.client = null;
            }
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("Rsh client close error.", ex);
        }
    }

    @Override
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    /**
     * 获取输入流
     *
     * @return 输入流
     */
    public InputStream getInputStream() {
        if (this.inputStream != null) {
            return this.inputStream;
        }
        if (this.client != null) {
            return this.client.getInputStream();
        }
        return null;
    }

    /**
     * 远端 pty 是否支持动态调整尺寸。
     *
     * @return 是否支持
     */
    public boolean isPtyResizeSupported() {
        return this.inputStream != null && this.inputStream.isResizeSupported();
    }

    /**
     * 获取输出流
     *
     * @return 输出流
     */
    public OutputStream getOutputStream() {
        if (this.client != null) {
            return this.client.getOutputStream();
        }
        return null;
    }
}
