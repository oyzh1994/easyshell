package cn.oyzh.easyssh.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SSHSftp {

    private ChannelSftp channel = null;

    public SSHSftp(ChannelSftp channel) {
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
