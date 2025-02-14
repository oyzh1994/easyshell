package cn.oyzh.easyssh.ssh;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.util.SSHShellUtil;
import com.jcraft.jsch.ChannelShell;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.Future;
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

    private Future<?> realtimeTask;

    @Getter
    @Setter
    private Consumer<SSHShellResult> onResponse;

    public SSHShell(ChannelShell shell) {
        this.shell = shell;
    }

    public void init() throws Exception {
        this.out = this.shell.getOutputStream();
        this.writer = new PrintWriter(this.out);
        this.shell.setPtyType("dumb");
        this.shell.connect();
        this.afterConnect();
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
            this.writer.flush();
            this.afterSend(command);
        }
    }

    /**
     * 连接以后业务
     *
     * @throws IOException 异常
     */
    protected void afterConnect() throws IOException {
        this.in = this.shell.getInputStream();
        String result = SSHShellUtil.readInput(this.in, 300, 100);
        if (this.onResponse != null) {
            this.responseForSingle(new SSHShellResult(result));
        }
    }

    /**
     * 命令发送以后业务
     *
     * @param command 命令
     * @throws IOException 异常
     */
    protected void afterSend(String command) throws IOException {
        // 获取输入流
        this.in = this.shell.getInputStream();
        // 读取结果
        String result = SSHShellUtil.readInput(this.in, 30, 500);
        // if (StringUtil.isEmpty(result)) {
        //     result = SSHShellUtil.readInput(this.in, 30, 500);
        // }
        // 处理响应
        if (this.onResponse != null && StringUtil.isNotEmpty(result)) {
            SSHShellResult shellResult = new SSHShellResult(command, result);
            // 单次
            if (shellResult.hasPrompt()) {
                this.responseForSingle(shellResult);
            } else {// 实时
                this.onResponse.accept(shellResult);
                this.responseForRealtime(command);
            }
        }
    }

    /**
     * 单次响应
     *
     * @param shellResult shell结果
     */
    private void responseForSingle(SSHShellResult shellResult) {
        this.onResponse.accept(shellResult);
    }

    /**
     * 实时响应
     *
     * @param command 指令
     */
    private void responseForRealtime(String command) {
        // 取消旧任务
        TaskManager.cancel(this.realtimeTask);
        // 创建任务
        this.realtimeTask = TaskManager.startInterval(() -> {
            try {
                String result = SSHShellUtil.readInput(this.in, -1, 500);
                SSHShellResult shellResult = new SSHShellResult(command, result);
                if (!this.realtimeTask.isCancelled()) {
                    this.onResponse.accept(shellResult);
                }
                if (shellResult.hasPrompt()) {
                    this.realtimeTask.cancel();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 100, 100);
    }

    /**
     * 发烧ctrl-c信号
     */
    public void sendCtrlCSignal() {
        try {
            this.out.write(new byte[]{3});
            this.out.flush();
            TimerUtil.cancel(this.realtimeTask);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        try {
            TaskManager.cancel(this.realtimeTask);
            this.shell.disconnect();
            IOUtil.close(this.in);
            IOUtil.close(this.out);
            IOUtil.close(this.writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
