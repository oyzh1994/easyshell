package cn.oyzh.easyshell.mosh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.mosh4j.core.MoshTerminalFrontend;

/**
 *
 * @author oyzh
 * @since 2026-07-06
 */
public class ShellMoshClient implements ShellBaseClient {

    /**
     * 连接状态
     */
    protected final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    /**
     * 当前状态监听器
     */
    protected final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellBaseClient.super.onStateChanged(state3);

    private ShellConnect shellConnect;

    public ShellMoshClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        ShellBaseClient.super.addStateListener(this.stateListener);
    }

    private MoshTerminalFrontend frontend;

    public MoshTerminalFrontend getFrontend() {
        return frontend;
    }

    public void setFrontend(MoshTerminalFrontend frontend) {
        this.frontend = frontend;
    }

    /**
     * 初始化客户端
     *
     * @param timeout 超时时间
     * @throws Exception 异常
     */
    private void initClient(int timeout) throws Exception {
        String moshKey = this.shellConnect.getMoshKey();
        if (StringUtil.isNotBlank(moshKey)) {
            this.frontend = ShellMoshHelper.connectWithMoshKey(this.shellConnect, moshKey);
        } else {
            this.shellConnect.setEnableCompress(true);
            this.frontend = ShellMoshHelper.connectWithSSH(this.shellConnect, timeout);
        }
    }

    @Override
    public void start(int timeout) throws Throwable {
        if (this.isConnected() || this.isConnecting()) {
            return;
        }
        try {
            // 开始连接时间
            long starTime = System.currentTimeMillis();
            // 初始化客户端
            if (this.frontend == null) {
                this.state.set(ShellConnState.CONNECTING);
                this.initClient(timeout);
                this.state.set(ShellConnState.CONNECTED);
            }
            // 添加到状态监听器队列
            if (this.isConnected()) {
                ShellClientChecker.push(this);
            }
            long endTime = System.currentTimeMillis();
            if (JulLog.isInfoEnabled()) {
                JulLog.info("Mosh client connected used:{}ms.", (endTime - starTime));
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            this.state.set(ShellConnState.FAILED);
            JulLog.warn("Mosh client start error", ex);
            throw new ShellException(ex);
        } finally {
            // 执行一次gc，快速回收内存
            SystemUtil.gc();
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.shellConnect;
    }

    @Override
    public boolean isConnected() {
        ShellConnState state = ShellBaseClient.super.getState();
        if (state != null && !state.isConnected()) {
            return false;
        }
        return this.frontend != null && this.frontend.isRunning();
    }

    @Override
    public void close() throws Exception {
        try {
            if (this.frontend != null) {
                this.frontend.close();
                this.frontend = null;
            }
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("Mosh client close error.", ex);
        }
    }

    /**
     * 设置终端大小
     *
     * @param columns 列
     * @param rows    行
     * @param sizeW   宽
     * @param sizeH   高
     */
    public void setPtySize(int columns, int rows, int sizeW, int sizeH) {
        if (this.isConnected()) {
            this.frontend.sendResize(columns, rows);
        }
    }

    /**
     * 发送用户输入
     *
     * @param bytes 输入
     */
    public void sendUserInput(byte[] bytes) {
        if (this.isConnected()) {
            this.frontend.sendUserInput(bytes);
        }
    }

    /**
     * 非阻塞拉取数据
     *
     * @return 结果 (可能为 null)
     */
    public byte[] pollHostBytes() {
        if (this.isConnected()) {
            return this.frontend.pollHostBytes();
        }
        return null;
    }

    /**
     * 阻塞拉取数据 (带超时)
     *
     * @param timeoutMs 超时毫秒
     * @return 结果
     * @throws InterruptedException 中断异常
     */
    public byte[] takeHostBytes(long timeoutMs) throws InterruptedException {
        if (this.isConnected()) {
            return this.frontend.takeHostBytes(timeoutMs);
        }
        return null;
    }

    /**
     * 发送心跳
     */
    public void sendHeartbeat() {
        if (this.isConnected()) {
            this.frontend.sendHeartbeat();
        }
    }

    /**
     * 阻塞拉取渲染数据 (带超时)
     *
     * @param timeoutMs 超时毫秒
     * @return 结果
     * @throws InterruptedException 中断异常
     */
    public String takeRenderedOutput(long timeoutMs) throws InterruptedException {
        if (this.isConnected()) {
            return this.frontend.takeRenderedOutput(timeoutMs);
        }
        return null;
    }
}
