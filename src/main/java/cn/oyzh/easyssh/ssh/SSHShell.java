package cn.oyzh.easyssh.ssh;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author oyzh
 * @since 2023/8/16
 */
public class SSHShell {

    private ChannelShell channel;

    public SSHShell(ChannelShell channel) {
        this.channel = channel;
    }

    public void close() {
        try {
            this.channel.disconnect();
            this.channel = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean isClosed() {
        return this.channel == null || this.channel.isClosed();
    }

    public void setPtySize(int columns, int rows, int sizeW, int sizeH) {
        this.channel.setPtySize(columns, rows, sizeW, sizeH);
    }

    public InputStream getInputStream() throws IOException {
        return this.channel.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.channel.getOutputStream();
    }

    public void connect(int connectTimeout) throws JSchException {
        this.channel.connect(connectTimeout);
    }

    public boolean isConnected() {
        return this.channel.isConnected();
    }
}
