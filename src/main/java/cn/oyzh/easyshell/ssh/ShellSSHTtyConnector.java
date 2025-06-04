package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.terminal.ShellDefaultTtyConnector;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellSSHTtyConnector extends ShellDefaultTtyConnector {

    /**
     * ssh客户端
     */
    private ShellSSHClient client;

    private InputStreamReader shellReader;

    private OutputStreamWriter shellWriter;

    public ShellSSHClient getClient() {
        return client;
    }

    public void initShell(ShellSSHClient client) throws IOException {
        this.client = client;
        ShellSSHShell shell = client.getShell();
        this.shellReader = new InputStreamReader(shell.getInputStream(), this.myCharset);
        this.shellWriter = new OutputStreamWriter(shell.getOutputStream(), this.myCharset);
    }

    public ShellSSHTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        int len;
        if (this.shellReader == null) {
            len = super.read(buf, offset, length);
        } else {
            len = this.shellReader.read(buf, offset, length);
        }
        if (len > 0) {
            this.doRead(buf, offset, len);
        }
        return len;
    }

    @Override
    public void write(String str) throws IOException {
        JulLog.debug("shell write : {}", str);
        this.shellWriter.write(str);
        this.shellWriter.flush();
//        this.saveTermHistory(str);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        String str = new String(bytes, this.myCharset);
        JulLog.debug("shell write : {}", str);
        this.shellWriter.write(str);
        this.shellWriter.flush();
//        this.saveTermHistory(str);
    }

    /**
     * 写入历史
     *
     * @param str 内容
     * @throws IOException 异常
     */
    public void writeHistory(String str) throws IOException {
        this.shellWriter.write(str + "\r");
        this.shellWriter.flush();
    }

//    /**
//     * 终端历史
//     */
//    private final StringBuilder termHistory = new StringBuilder();

//    /**
//     * 保存终端历史
//     *
//     * @param output 输出
//     */
//    private void saveTermHistory(String output) {
//        if (this.client != null) {
//            this.termHistory.append(output);
//            String command = this.termHistory.toString();
//            // 针对回显字符，忽略
//            if (command.contains("@") && StringUtil.containsAny(command, "% ", "# ", "@ ", ">")) {
//                this.termHistory.setLength(0);
//                return;
//            }
//            // 针对首次打开，出现登陆提示，忽略
//            if (StringUtil.containsAny(command, "登陆:", "login:")) {
//                this.termHistory.setLength(0);
//                return;
//            }
//            // 针对macos设置字符集，忽略
//            if (StringUtil.containsAny(command, "export LANG=")) {
//                this.termHistory.setLength(0);
//                return;
//            }
//            if (StringUtil.containsAny(command, "\r", "\n") || StringUtil.endsWithAny(output, "\r", "\n")) {
//                command = command.lines().findFirst().get();
//                JulLog.error("term history : {}", command);
//                this.termHistory.setLength(0);
//                this.client.saveTermHistory(command);
//            }
//        }
//    }

    @Override
    public void close() {
        super.close();
        this.client = null;
        if (this.shellReader != null) {
            IOUtil.close(this.shellReader);
            this.shellReader = null;
        }
        if (this.shellWriter != null) {
            IOUtil.close(this.shellWriter);
            this.shellWriter = null;
        }
    }

    @Override
    protected void doRead(char[] buf, int offset, int len) throws IOException {
        super.doRead(buf, offset, len);
        if (this.client != null) {
            ThreadUtil.startVirtual(() -> this.client.resolveWorkerDir(new String(buf, offset, len)));
        }
    }
}