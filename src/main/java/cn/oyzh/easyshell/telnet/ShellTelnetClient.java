package cn.oyzh.easyshell.telnet;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;
import cn.oyzh.easyshell.internal.ShellConnState;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author oyzh
 * @since 2025-04-24
 */
public class ShellTelnetClient implements BaseClient {

    /**
     * 客户端
     */
    private TelnetClient client;

    /**
     * 连接
     */
    private final ShellConnect shellConnect;

    /**
     * 连接状态
     */
    private final ReadOnlyObjectWrapper<ShellConnState> state = new ReadOnlyObjectWrapper<>();

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> BaseClient.super.onStateChanged(state3);

    @Override
    public ReadOnlyObjectProperty<ShellConnState> stateProperty() {
        return this.state.getReadOnlyProperty();
    }

    public ShellTelnetClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    /**
     * 初始化客户端
     */
    private void initClient() {
        this.client = new TelnetClient();
        this.client.setCharset(BaseClient.super.getCharset());
    }

    @Override
    public void start(int timeout) throws IOException {
        this.initClient();
        this.client.setConnectTimeout(timeout);
        try {
            this.state.set(ShellConnState.CONNECTING);
            this.client.connect(this.shellConnect.hostIp(), this.shellConnect.hostPort());
            if (this.client.isConnected()) {
                this.state.set(ShellConnState.CONNECTED);
            } else {
                this.state.set(ShellConnState.FAILED);
            }
        } catch (Exception ex) {
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
            this.state.removeListener(this.stateListener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private WindowSizeOptionHandler sizeHandler;

    public void setPtySize(int cols, int rows) {
        try {
            if (this.sizeHandler != null) {
                this.client.deleteOptionHandler(this.sizeHandler.getOptionCode());
                this.sizeHandler = null;
            }
            this.sizeHandler = new WindowSizeOptionHandler(cols, rows, true, true, true, true);
            this.client.addOptionHandler(this.sizeHandler);
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
