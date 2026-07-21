package cn.oyzh.easyshell.vnc1;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.vnc.VncClipboardHandler;
import cn.oyzh.fx.vnc.VncFramebufferView;
import com.glavsoft.rfb.IRfbSessionListener;
import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.rfb.protocol.Protocol;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.rfb.protocol.tunnel.TunnelType;
import com.glavsoft.transport.BaudrateMeter;
import com.glavsoft.transport.Transport;
import com.glavsoft.viewer.settings.LocalMouseCursorShape;
import com.glavsoft.viewer.settings.UiSettings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * vnc客户端
 *
 * @author oyzh
 * @since 2025-05-23
 */
public class ShellVNCClient implements ShellBaseClient, IRfbSessionListener {


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

    public ShellVNCClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }


    /**
     * socket对象
     */
    private Socket socket;

    /**
     * 协议
     */
    private Protocol protocol;

    /**
     * ui设置
     */
    private UiSettings uiSettings;

    /**
     * 协议设置
     */
    private ProtocolSettings protocolSettings;

    /**
     * 剪切板处理器
     */
    private VncClipboardHandler clipboardHandler;

    /**
     * 初始化客户端
     */
    protected void initClient() throws IOException {
        String hostIp = this.shellConnect.hostIp();
        int hostPort = this.shellConnect.hostPort();
        int connectTimeOut = this.shellConnect.connectTimeOutMs();

        // 处理代理
        if (this.shellConnect.isEnableProxy()) {
            this.socket = ShellProxyUtil.createSocket(this.shellConnect.getProxyConfig(), hostIp, hostPort, connectTimeOut);
        } else {
            this.socket = new Socket();
            this.socket.setKeepAlive(true);
            this.socket.connect(new InetSocketAddress(hostIp, hostPort), connectTimeOut);
            this.socket.setTcpNoDelay(true);
        }

        this.uiSettings = new UiSettings();
        this.protocolSettings = ProtocolSettings.getDefaultSettings();
        this.protocolSettings.setSharedFlag(true);
        this.protocolSettings.setAllowCopyRect(true);
//        this.protocolSettings.setJpegQuality(1);
//        this.protocolSettings.setCompressionLevel(9);
        this.protocolSettings.setAllowClipboardTransfer(true);
        // ssl模式
        if (this.shellConnect.isSSLMode()) {
            this.protocolSettings.setTunnelType(TunnelType.SSL);
        }
        // ZRLE容易发生数据损坏
        this.protocolSettings.setPreferredEncoding(EncodingType.TIGHT);
//        this.protocolSettings.setPreferredEncoding(EncodingType.ZRLE);

        // Setup transport
        Transport transport = new Transport(this.socket);
        transport.setBaudrateMeter(new BaudrateMeter());
        // Create protocol
        this.protocol = new Protocol(transport, () -> this.shellConnect.getPassword() != null ? this.shellConnect.getPassword() : "", protocolSettings);
    }

    /**
     * 初始化vnc组件
     *
     * @param vncView vnc组件
     */
    public void initVncView(VncFramebufferView vncView) {
        // 初始化视图组件
        FXUtil.runLater(() -> vncView.init(this.protocol, this.uiSettings.getScaleFactor(), LocalMouseCursorShape.NO_CURSOR));

        // Setup settings
        this.uiSettings.addListener(vncView);
        this.protocolSettings.addListener(vncView);

        // Setup clipboard
        String encoding = StringUtil.blankToDefault(this.shellConnect.getCharset(), "ISO-8859-1");
        this.clipboardHandler = new VncClipboardHandler(this.protocol, encoding);

        this.protocolSettings.addListener(this.clipboardHandler);
        this.protocol.startNormalHandling(this, vncView, this.clipboardHandler);

        // Start clipboard polling
        this.clipboardHandler.setEnabled(true);
    }

    @Override
    public void start(int timeout) throws Exception {
        if (this.isConnected()) {
            return;
        }
        this.initClient();
        try {
            this.state.set(ShellConnState.CONNECTING);
            // 开始连接
            this.protocol.handshake();
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
            if (this.protocol != null) {
                this.protocol.destroy();
                this.protocol = null;
            }
            if (this.uiSettings != null) {
                this.uiSettings.clearListener();
                this.uiSettings = null;
            }
            if (this.protocolSettings != null) {
                this.protocolSettings.clearListener();
                this.protocolSettings = null;
            }
            if (this.clipboardHandler != null) {
                this.clipboardHandler.destroy();
                this.clipboardHandler = null;
            }
            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
            this.stateProperty().unbind();
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
        if (this.state.get() == ShellConnState.CLOSED) {
            return false;
        }
        return this.socket != null && this.socket.isConnected();
    }

    @Override
    public void rfbSessionStopped(String reason) {
        this.state.set(ShellConnState.CLOSED);
    }

    /**
     * 缩放到合适比例
     *
     * @param width  容器宽
     * @param height 容器高
     * @param fbWidth         渲染宽
     * @param fbHeight        渲染高
     */
    public void zoomToFit(int width, int height, int fbWidth, int fbHeight) {
        if (this.uiSettings == null) {
            return;
        }
        if (fbWidth == 0 || fbHeight == 0) {
            return;
        }
        this.uiSettings.zoomToFit(width, height, fbWidth, fbHeight);
    }
}
