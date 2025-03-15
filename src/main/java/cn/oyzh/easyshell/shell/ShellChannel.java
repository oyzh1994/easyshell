package cn.oyzh.easyshell.shell;

import cn.oyzh.common.thread.TaskManager;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author oyzh
 * @since 2025/03/08
 */
public class ShellChannel implements AutoCloseable {

    private Channel channel;

    public ShellChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public void run() {

    }

    @Override
    public void close() {
        if (this.channel != null) {
            TaskManager.startTimeout(() -> {
                try {
                    this.channel.disconnect();
                    this.channel = null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }, 1000);
        }
    }

    public boolean isClosed() {
        return this.channel == null || this.channel.isClosed();
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isConnected();
    }

    public void connect() throws JSchException {
        if (!this.isConnected()) {

            this.channel.connect();
        }
    }

    public void connect(int connectTimeout) throws JSchException {
        if (!this.isConnected()) {
            this.channel.connect(connectTimeout);
        }
    }

    public InputStream getInputStream() throws IOException {
        return this.channel.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.channel.getOutputStream();
    }

    public Session getSession() throws JSchException {
        return this.channel.getSession();
    }

    public boolean isEOF() {
        return this.channel.isEOF();
    }

    public void disconnect() {
        this.close();
    }

    public void sendSignal(String signal) throws Exception {
        this.channel.sendSignal(signal);
    }

    public int getExitStatus() {
        return this.channel.getExitStatus();
    }

    public int getId() {
        return this.channel.getId();
    }

    public InputStream getExtInputStream() throws IOException {
        return this.channel.getExtInputStream();
    }

    public void setXForwarding(boolean xForwarding) {
        this.channel.setXForwarding(xForwarding);
    }

    public void setExtOutputStream(OutputStream stream) {
        this.channel.setExtOutputStream(stream);
    }

    public void setExtOutputStream(OutputStream stream, boolean dontclose) {
        this.channel.setExtOutputStream(stream, dontclose);
    }
}
