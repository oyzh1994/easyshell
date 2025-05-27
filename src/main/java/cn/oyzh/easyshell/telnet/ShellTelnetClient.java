package cn.oyzh.easyshell.telnet;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;
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

    public ShellTelnetClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    private void initClient() {
        this.client = new TelnetClient();
        this.client.setCharset(BaseClient.super.getCharset());
    }

    @Override
    public void start(int timeout) throws IOException {
        this.initClient();
        this.client.setConnectTimeout(timeout);
        this.client.connect(this.shellConnect.hostIp(), this.shellConnect.hostPort());
    }

    @Override
    public void close() {
        try {
            if (this.client != null) {
                this.client.disconnect();
                this.client = null;
            }
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
