package cn.oyzh.easyshell.rlogin;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.BaseClient;
import org.apache.commons.net.bsd.RCommandClient;
import org.apache.commons.net.bsd.RLoginClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

/**
 * @author oyzh
 * @since 2025-05-27
 */
public class ShellRLoginClient implements BaseClient {

    private RLoginClient client;

    private final ShellConnect shellConnect;

    public ShellRLoginClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
    }

    private void initClient() {
        this.client = new RLoginClient();
        RCommandClient.MAX_CLIENT_PORT = 10086;
        this.client.setSocketFactory(new ShellRLoginSocketFactory());
    }

    @Override
    public void start(int timeout) throws IOException {
        this.initClient();
        this.client.setConnectTimeout(timeout);
        this.client.setCharset(BaseClient.super.getCharset());

        InetAddress local = InetAddress.getLocalHost();

        this.client.connect(this.shellConnect.hostIp(), this.shellConnect.hostPort());
//        this.client.connect(this.shellConnect.hostIp(), this.shellConnect.hostPort(), local, 11231);
        String user = this.shellConnect.getUser();
        String termType = this.shellConnect.getTermType();
        this.client.rlogin("", user, termType);
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
