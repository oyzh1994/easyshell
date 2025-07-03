package cn.oyzh.easyshell.rlogin;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.apache.commons.net.bsd.RLoginClient;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author oyzh
 * @since 2025-05-27
 */
public class ShellRLoginClient implements ShellBaseClient {

    /**
     * 客户端
     */
    private RLoginClient client;

    /**
     * 连接
     */
    private final ShellConnect shellConnect;

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellBaseClient.super.onStateChanged(state3);

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    public ShellRLoginClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    /**
     * 初始化客户端
     */
    private void initClient() {
        this.client = new RLoginClient();
        this.client.setCharset(ShellBaseClient.super.getCharset());
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
            DownLatch latch = DownLatch.of();
            // 丢进线程执行，避免一直卡住
            ThreadUtil.startWithError(() -> this.client.connect(this.shellConnect.hostIp(), this.shellConnect.hostPort()), latch::countDown);
            // 等待结束
            if (!latch.await(timeout)) {
                this.state.set(ShellConnState.FAILED);
                return;
            }
            String user = this.shellConnect.getUser();
            String termType = this.shellConnect.getTermType();
            this.client.rlogin(user, user, termType);
            if (this.client.isConnected()) {
                this.state.set(ShellConnState.CONNECTED);
                // 添加到状态监听器队列
                ShellClientChecker.push(this);
            } else {
                this.state.set(ShellConnState.FAILED);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
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
            if (this.client != null) {
                this.client.disconnect();
                this.client = null;
            }
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
//            this.shellConnect = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    public InputStream getInputStream() {
        if (this.client != null) {
            return this.client.getInputStream();
        }
        return null;
    }

    public OutputStream getOutputStream() {
        if (this.client != null) {
            return this.client.getOutputStream();
        }
        return null;
    }

}
