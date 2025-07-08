package cn.oyzh.easyshell.vnc;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.jfxvnc.net.rfb.VncConnection;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.jfxvnc.net.rfb.render.RenderProtocol;
import org.jfxvnc.ui.service.VncRenderService;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * vnc客户端
 *
 * @author oyzh
 * @since 2025-05-23
 */
public class ShellVNCClient implements ShellBaseClient {

    /**
     * 空渲染组件
     */
    public static final VncRenderService NO_OP = new VncRenderService();

    /**
     * vnc连接
     */
    private VncConnection connection;

    /**
     * 渲染组件
     */
    private RenderProtocol renderProtocol;

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

    public ShellVNCClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    /**
     * 初始化客户端
     */
    protected void initClient() {
        // 创建连接
        this.connection = new VncConnection();
        // 设置渲染组件
        this.connection.setRenderProtocol(Objects.requireNonNullElse(this.renderProtocol, NO_OP));
        // 错误处理
        this.connection.addFaultListener(MessageBox::exception);
        // 配置
        ProtocolConfiguration config = this.connection.getConfiguration();
        // 基础属性
        config.sharedProperty().set(true);
        config.rawEncProperty().set(true);
        config.zlibEncProperty().set(true);
        config.hextileEncProperty().set(true);
        config.copyRectEncProperty().set(true);
        config.desktopSizeProperty().set(true);
        config.clientCursorProperty().set(true);
        // ssl模式
        if (this.shellConnect.isSSLMode()) {
            config.sslProperty().set(true);
        }
        config.hostProperty().set(this.shellConnect.hostIp());
        config.portProperty().set(this.shellConnect.hostPort());
        config.passwordProperty().set(this.shellConnect.getPassword());
    }

    @Override
    public void start(int timeout) throws Exception {
        if (this.isConnected()) {
            return;
        }
        this.initClient();
        try {
            this.state.set(ShellConnState.CONNECTING);
            CompletableFuture<VncConnection> future = this.connection.connect();
            future.get(timeout, TimeUnit.MILLISECONDS);
            if (this.isConnected()) {
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
            if (this.connection != null) {
                this.connection.disconnect();
                this.connection = null;
            }
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
//            this.shellConnect = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    @Override
    public boolean isConnected() {
        return this.connection != null && this.connection.isConnected();
    }

    public void setRenderProtocol(RenderProtocol renderProtocol) {
        this.renderProtocol = renderProtocol;
    }
}
