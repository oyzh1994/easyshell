package cn.oyzh.easyshell.rlogin;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;
import org.apache.commons.net.bsd.RLoginClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author oyzh
 * @since 2025-05-27
 */
public class ShellRLoginClient implements BaseClient {

    /**
     * 客户端
     */
    private RLoginClient client;

    /**
     * 连接
     */
    private final ShellConnect shellConnect;

    public ShellRLoginClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    private void initClient() {
        this.client = new RLoginClient();
        this.client.setCharset(BaseClient.super.getCharset());
    }

    @Override
    public void start(int timeout) throws IOException {
        this.initClient();
        this.client.setConnectTimeout(timeout);
        this.client.connect(this.shellConnect.hostIp(), this.shellConnect.hostPort());
        String user = this.shellConnect.getUser();
        String termType = this.shellConnect.getTermType();
        this.client.rlogin(user, user, termType);
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
