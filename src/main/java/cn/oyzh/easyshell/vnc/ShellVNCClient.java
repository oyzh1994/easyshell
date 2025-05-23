package cn.oyzh.easyshell.vnc;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.fx.plus.information.MessageBox;
import org.jfxvnc.net.rfb.VncConnection;
import org.jfxvnc.net.rfb.render.ProtocolConfiguration;
import org.jfxvnc.net.rfb.render.RenderProtocol;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author oyzh
 * @since 2025-05-23
 */
public class ShellVNCClient implements BaseClient {


    private VncConnection connection;

    private RenderProtocol renderProtocol;

    private final ShellConnect shellConnect;

    public ShellVNCClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    /**
     * 初始化串口
     */
    protected void initClient() {
        // 获取指定名称的串口
        this.connection = new VncConnection();
        this.connection.setRenderProtocol(this.renderProtocol);
        this.connection.addFaultListener(ex -> {
            ex.printStackTrace();
            MessageBox.exception(ex);
        });
        ProtocolConfiguration config = this.connection.getConfiguration();
        config.sharedProperty().set(true);
        config.rawEncProperty().set(true);
        config.copyRectEncProperty().set(true);
        config.desktopSizeProperty().set(true);
        config.clientCursorProperty().set(true);
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
        CompletableFuture<VncConnection> future = this.connection.connect();
        future.get(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        try {
            if (this.connection != null) {
                this.connection.disconnect();
            }
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
        if (this.connection != null) {
            return this.connection.isConnected();
        }
        return false;
    }

    public void setRenderProtocol(RenderProtocol renderProtocol) {
        this.renderProtocol = renderProtocol;
    }



}
