package cn.oyzh.easyshell.ssh;

import com.jcraft.jsch.ChannelShell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author oyzh
 * @since 2023/8/16
 */
public class ShellSSHShell extends ShellSSHChannel {

    /**
     * Shell 输入流，必须在通道连接前创建并持续复用。
     */
    private InputStream inputStream;

    /**
     * Shell 输出流，必须在通道连接前创建并持续复用。
     */
    private OutputStream outputStream;

    public ShellSSHShell(ChannelShell channel) {
        super(channel);
    }

    public ShellSSHShell(ChannelShell channel, InputStream inputStream, OutputStream outputStream) {
        super(channel);
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public ChannelShell getChannel() {
        return (ChannelShell) super.getChannel();
    }

    public void setPtySize(int columns, int rows, int sizeW, int sizeH) {
        this.getChannel().setPtySize(columns, rows, sizeW, sizeH);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (this.inputStream != null) {
            return this.inputStream;
        }
        return super.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (this.outputStream != null) {
            return this.outputStream;
        }
        return super.getOutputStream();
    }

    @Override
    public void close() {
        super.close();
        this.inputStream = null;
        this.outputStream = null;
    }
}
