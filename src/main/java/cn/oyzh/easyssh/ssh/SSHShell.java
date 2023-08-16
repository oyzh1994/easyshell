package cn.oyzh.easyssh.ssh;

import cn.oyzh.easyssh.util.SSHConnectUtil;
import com.jcraft.jsch.ChannelShell;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2023/8/16
 */
public class SSHShell {

    private InputStream in;

    private OutputStream out;

    private PrintWriter writer;

    private final ChannelShell shell;

    @Getter
    @Setter
    private Consumer<SSHShellResult> onResponse;

    public SSHShell(ChannelShell shell) {
        this.shell = shell;
    }

    public void init() throws Exception {
        if (this.in == null || !this.shell.isConnected()) {
            this.in = this.shell.getInputStream();
            this.out = this.shell.getOutputStream();
            this.writer = new PrintWriter(this.out, true);
            this.shell.setPty(true);
            this.shell.setPtyType("dumb");
            this.shell.connect();
            this.afterConnect();
        }
    }

    /**
     * 发送命令
     *
     * @param command 命令
     * @throws Exception 异常
     */
    public void send(String command) throws Exception {
        if (command != null) {
            this.writer.println(command);
            this.afterSend(command);
        }
    }

    /**
     * 连接以后业务
     *
     * @throws IOException 异常
     */
    protected void afterConnect() throws IOException {
        String result = SSHConnectUtil.readShellInput(this.in, 300, 100);
        if (this.onResponse != null) {
            this.onResponse.accept(new SSHShellResult(result));
        }
    }

    /**
     * 命令发送以后业务
     *
     * @param command 命令
     * @throws IOException 异常
     */
    protected void afterSend(String command) throws IOException {
        String result = SSHConnectUtil.readShellInput(this.in, 30, 500);
        if (this.onResponse != null) {
            this.onResponse.accept(new SSHShellResult(command, result));
        }
    }

    public void close() {
        try {
            this.shell.disconnect();
            this.in.close();
            this.out.close();
            this.writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
