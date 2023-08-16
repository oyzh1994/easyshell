package cn.oyzh.easyssh.ssh;

import cn.oyzh.easyssh.util.SSHConnectUtil;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
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

    private void init() throws IOException, JSchException {
        if (this.in == null || !this.shell.isConnected()) {
            this.in = this.shell.getInputStream();
            this.out = this.shell.getOutputStream();
            this.writer = new PrintWriter(this.out);
            this.shell.setPty(true);
            this.shell.setPtyType("dumb");
            this.shell.connect();
        }
    }

    public void send(String command) throws Exception {
        this.init();
        this.writer.println(command);
        this.writer.flush();
        if (this.onResponse != null) {
            String result = SSHConnectUtil.readShellInput(this.in);
            SSHShellResult shellResult = new SSHShellResult(command, result);
            this.onResponse.accept(shellResult);
        }
    }

    public void close() {
        try {
            this.shell.disconnect();
            this.out.close();
            this.writer.close();
            this.in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
