package cn.oyzh.easyssh.shell;

import cn.oyzh.common.thread.TimerUtil;
import cn.oyzh.easyfx.util.FXUtil;
import cn.oyzh.easyshell.execute.ShellExecuteResult;
import cn.oyzh.easyshell.terminal.ShellTerminal;
import cn.oyzh.easyshell.terminal.ShellTerminalTextArea;
import cn.oyzh.easyssh.dto.SSHConnect;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.ssh.SSHConnState;
import cn.oyzh.easyssh.ssh.SSHShell;
import cn.oyzh.easyssh.util.SSHConnectUtil;
import javafx.beans.value.ChangeListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * ssh终端
 *
 * @author oyzh
 * @since 2023/7/21
 */
@Slf4j
public class SSHShellTerminalTextArea extends ShellTerminalTextArea {

    {
        this.setLineCompleter(SSHShellCompleter.INSTANCE);
        this.setHistoryStore(SSHShellHistoryStore.INSTANCE);
    }

    /**
     * ssh客户端
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    private SSHClient client;

    /**
     * 交互式终端
     */
    private SSHShell shell;

    /**
     * ssh客户端连接状态监听器
     */
    private ChangeListener<SSHConnState> connStateChangeListener;


    @Override
    public void flushPrompt() {
        if (!this.client.isConnected()) {
            this.promptContent("ssh连接@" + this.client.infoName() + "> ");
        } else {
            this.promptContent("ssh连接@" + this.client.infoName() + "（已连接）> ");
        }
    }

    /**
     * 初始化
     *
     * @param client ssh客户端
     */
    public void init(@NonNull SSHClient client) {
        this.client = client;
        this.disableInput();
        this.appendLine("欢迎使用EasySSH!");
        this.appendLine("Powered By oyzh(2023-2023).");
        if (this.isTemporary()) {
            this.initByTemporary();
        } else {
            this.initByPermanent();
        }
    }

    /**
     * 是否临时连接
     *
     * @return 结果
     */
    public boolean isTemporary() {
        return this.client.sshInfo().getId() == null;
    }

    @Override
    public void appendPrompt() {
        if (!this.client.isConnecting()) {
            super.appendPrompt();
        }
    }

    @Override
    public boolean onEnterKeyPressed(ShellTerminal terminal) {
        if (this.isTemporary() && !this.isConnected()) {
            String input = super.getInput();
            this.connect(input);
            super.saveHistory(input);
        } else if (!this.isConnecting()) {
            super.onEnterKeyPressed(terminal);
        }
        return false;
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    /**
     * 是否连接中
     *
     * @return 结果
     */
    public boolean isConnecting() {
        return this.client != null && this.client.isConnecting();
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    public boolean isClosed() {
        return this.client != null && this.client.isClosed();
    }

    /**
     * 执行连接
     *
     * @param input 输入内容
     */
    private void connect(String input) {
        this.client.reset();
        SSHConnect connect = SSHConnectUtil.parse(input);
        if (connect != null) {
            try {
                this.client.sshInfo().setHost(connect.getHost() + ":" + connect.getPort());
                this.client.sshInfo().setPassword(connect.getPassword());
                this.intConnStat();
                this.disable();
                this.client.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                this.enable();
            }
        }
    }

    /**
     * 临时连接处理
     */
    private void initByTemporary() {
        this.appendLine("请输入连接地址然后回车，格式-h host [-p port] [-a password] [-n db]");
        this.appendText("-h 127.0.0.1");
        TimerUtil.start(() -> {
            FXUtil.runLater(this::requestFocus);
            this.enableInput();
            this.flushCaret();
            this.moveCaretEnd();
        }, 10);
    }

    /**
     * 常驻连接处理
     */
    private void initByPermanent() {
        this.appendLine(this.client.sshInfo().getHost() + " 连接开始.");
        TimerUtil.start(() -> {
            try {
                this.intConnStat();
                this.client.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 10);
    }

    /**
     * 初始化连接状态处理
     */
    private void intConnStat() {
        if (this.connStateChangeListener == null) {
            this.connStateChangeListener = (observableValue, state, t1) -> {
                this.flushPrompt();
                // 获取连接
                String host = this.client.sshInfo().getHost();
                if (t1 == SSHConnState.CONNECTED) {
                    this.outputByAppend(host + " 连接成功.");
                    this.outputByAppend("输入help可查看支持的命令列表.");
                    this.appendPrompt();
                    this.flushCaret();
                    super.enableInput();
                } else if (t1 == SSHConnState.CLOSED) {
                    this.disableInput();
                    this.outputByAppend(host + " 连接关闭.");
                } else if (t1 == SSHConnState.FAILED) {
                    this.disableInput();
                    this.outputByAppend(host + " 连接失败.");
                    this.flushCaret();
                }
                log.info("connState={}", t1);
            };
        }
        this.client().addConnStateListener(this.connStateChangeListener);
    }

    @Override
    public void enableInput() {
        if (this.isConnected() || this.isTemporary()) {
            super.enableInput();
        }
    }

    @Override
    public void onCommand(String input) throws RuntimeException {
        try {
            if (this.shell == null) {
                this.shell = this.client.shell();
                this.shell.setOnResponse(shellResult -> {
                    ShellExecuteResult result = new ShellExecuteResult();
                    result.setResult(shellResult.getClipResult());
                    if (shellResult.getPrompt() != null) {
                        this.promptContent(shellResult.getPrompt());
                    }
                    if (result.isSuccess()) {
                        this.outputByPrompt((String) result.getResult());
                    } else {
                        this.outputByPrompt(result.getErrMsg());
                    }
                });
            }
            this.shell.send(input);
        } catch (Exception e) {
            e.printStackTrace();
            this.outputByPrompt("初始化交互终端失败");
        }
    }
}
