package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.dto.zk.ShellZKConnectInfo;
import cn.oyzh.easyshell.exception.ShellExceptionParser;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.terminal.ShellTerminalHistoryHandler;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.zk.ShellZKConnectUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.terminal.TerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.util.TerminalManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.scene.text.Font;
import org.apache.zookeeper.ZooKeeper;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 基于动态命令发现的 ZooKeeper v2 终端。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class ZKTerminalPane extends TerminalPane {

    /** ZooKeeper v2 终端名称。 */
    public static final String TERMINAL_NAME = "zookeeper-v2";

    /** 不需要 ZooKeeper 连接即可执行的 CLI 命令。 */
    private static final Set<String> CLI_COMMANDS_WITHOUT_CONNECTION = Set.of("version");

    /** ZooKeeper 命令执行器。 */
    private final ZKCommandExecutor commandExecutor = new ZKCommandExecutor();

    /** ZooKeeper 客户端。 */
    private ShellZKClient client;
    /** 临时连接解析结果。 */
    private ShellZKConnectInfo connectInfo;
    /** 客户端连接状态监听器。 */
    private ChangeListener<ShellConnState> stateChangeListener;

    @Override
    public Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.terminalFontConfig());
    }

    /**
     * 获取 ZooKeeper 客户端。
     *
     * @return ZooKeeper 客户端
     */
    public ShellZKClient getClient() {
        return client;
    }

    /**
     * 获取底层 ZooKeeper 会话对象。
     *
     * @return ZooKeeper 会话
     * @throws Exception 获取会话失败
     */
    public ZooKeeper zooKeeper() throws Exception {
        return this.client.getZooKeeper();
    }

    @Override
    public void flushPrompt() {
        String prompt;
        if (this.isTemporary()) {
            prompt = "zk" + I18nHelper.connect();
        } else {
            prompt = this.client.connectName();
        }
        if (this.shellConnect() != null && this.shellConnect().getHost() != null) {
            prompt += "@" + this.shellConnect().getHost();
        }
        if (this.isConnecting()) {
            prompt += "(" + I18nHelper.connectIng() + ")> ";
        } else if (this.isConnected()) {
            if (this.client.isReadonly()) {
                prompt += "(" + I18nHelper.connected() + "/" + I18nHelper.readonlyMode() + ")> ";
            } else {
                prompt += "(" + I18nHelper.connected() + ")> ";
            }
        } else {
            prompt += "> ";
        }
        this.prompt(prompt);
    }

    @Override
    public String terminalName() {
        return TERMINAL_NAME;
    }

    /**
     * 初始化 ZooKeeper 终端。
     *
     * @param client ZooKeeper 客户端
     */
    public void init(ShellZKClient client) {
        this.client = client;
        FXUtil.runPulse(() -> {
            this.disableInput();
            this.outputLine(ShellI18nHelper.welcome());
            this.outputLine("Powered By oyzh(2022-2026).");
            this.flushPrompt();
            if (this.isTemporary()) {
                this.initByTemporary();
            } else {
                this.initByPermanent();
            }
        });
    }

    /**
     * 判断当前是否为临时连接终端。
     *
     * @return 是否为临时终端
     */
    public boolean isTemporary() {
        return this.client == null || this.client.iid() == null;
    }

    @Override
    public void outputPrompt() {
        if (!this.isConnecting()) {
            super.outputPrompt();
        }
    }

    /**
     * 判断 ZooKeeper 是否已连接。
     *
     * @return 是否已连接
     */
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    /**
     * 判断 ZooKeeper 是否正在连接。
     *
     * @return 是否正在连接
     */
    public boolean isConnecting() {
        return this.client != null && this.client.isConnecting();
    }

    /**
     * 判断 ZooKeeper 连接是否已关闭。
     *
     * @return 是否已关闭
     */
    public boolean isClosed() {
        return this.client != null && this.client.isClosed();
    }

    /**
     * 按终端输入解析并建立临时 ZooKeeper 连接。
     *
     * @param input connect 命令内容
     */
    public void connect(String input) {
        this.connectInfo = ShellZKConnectUtil.parse(input);
        if (this.connectInfo != null) {
            this.disable();
            ShellZKConnectUtil.copyConnect(this.connectInfo, this.shellConnect());
            this.start();
        }
    }

    /**
     * 初始化临时终端的连接提示。
     */
    private void initByTemporary() {
        this.outputLine("connect [-timeout timeout] [-server server] [-r]");
        this.outputLine("-timeout " + I18nResourceBundle.i18nString("base.unit", "base.ms"));
        this.outputLine("-server ip:" + I18nHelper.port());
        this.outputLine("-r " + I18nHelper.readonlyMode());
        this.appendByPrompt("connect -timeout 3000 -server localhost:2181");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    /**
     * 初始化固定连接终端。
     */
    private void initByPermanent() {
        this.flushPrompt();
        this.appendByPrompt("");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    /**
     * 异步启动 ZooKeeper 客户端。
     */
    private void start() {
        TaskManager.startSync(() -> {
            try {
                this.initStateListener();
                this.client.start();
            } catch (Throwable ex) {
                this.onError(ShellExceptionParser.INSTANCE.apply(ex));
            } finally {
                this.enable();
            }
        });
    }

    /**
     * 注册 ZooKeeper 连接状态监听器。
     */
    private void initStateListener() {
        if (this.stateChangeListener != null) {
            return;
        }
        this.stateChangeListener = (observableValue, state, newState) -> {
            this.flushPrompt();
            String host = this.shellConnect() == null ? "" : this.shellConnect().getHost();
            if (newState == ShellConnState.CONNECTED) {
                this.outputLine(host + I18nHelper.connectSuccess() + " .");
                this.outputLine(I18nHelper.terminalTip2());
                this.outputLine(I18nHelper.terminalTip1());
                this.outputPrompt();
                this.flushCaret();
                super.enableInput();
            } else if (newState == ShellConnState.CLOSED) {
                this.outputLine(host + " " + I18nHelper.connectionClosed() + " .");
                this.enableInput();
            } else if (newState == ShellConnState.CONNECTING) {
                this.outputLine(host + " " + I18nHelper.connectionConnecting() + " .", false);
            } else if (newState == ShellConnState.INTERRUPTED) {
                this.outputLine(host + " " + I18nHelper.connectSuspended() + " .");
                this.enableInput();
            } else if (newState == ShellConnState.RECONNECTED) {
                this.outputLine(host + " " + I18nHelper.connectReconnected() + " .");
                this.outputPrompt();
                this.flushCaret();
                super.enableInput();
            } else if (newState == ShellConnState.FAILED) {
                this.outputLine(host + I18nHelper.connectFail() + " .");
                if (this.connectInfo != null) {
                    this.appendByPrompt(this.connectInfo.getInput());
                }
                this.flushAndMoveCaretEnd();
                this.enableInput();
            }
            JulLog.info("connState={}", newState);
        };
        this.client.addStateListener(this.stateChangeListener);
    }

    @Override
    public void enableInput() {
        if (this.isConnecting()) {
            return;
        }
        if (this.isConnected() || this.isTemporary()) {
            super.enableInput();
        }
    }

    public ShellConnect shellConnect() {
        return this.client == null ? null : this.client.getShellConnect();
    }

    /**
     * 查找静态命令处理器，其余输入交给动态命令处理器。
     */
    @Override
    protected TerminalCommandHandler findHandler(String input) {
        TerminalCommandHandler<?, ?> handler = TerminalManager.findHandler(TERMINAL_NAME, input);
        if (handler != null) {
            return handler;
        }
        List<String> words = ZKCommandTokenizer.tokenize(input);
        String commandName = words.isEmpty() ? input : words.getFirst();
        return new ZKDynamicCommandHandler(commandName);
    }

    /**
     * 解析并执行一行终端命令。
     *
     * @param input 命令行内容
     * @return 执行结果
     */
    public TerminalExecuteResult eval(String input) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            TerminalCommandHandler handler = this.findHandler(input);
            TerminalCommand command = handler.parseCommand(input);
            return handler.execute(command, this);
        } catch (Exception ex) {
            result.setException(ex);
            return result;
        }
    }

    /**
     * 执行完整 ZooKeeper 命令参数。
     *
     * @param args 完整命令参数
     * @return 执行结果
     */
    public TerminalExecuteResult executeCommand(List<String> args) {
        TerminalExecuteResult terminalResult = TerminalExecuteResult.ok();
        if (args == null || args.isEmpty()) {
            terminalResult.setErrMsg("No command entered");
            return terminalResult;
        }
        String commandName = args.getFirst();
        ZKCommandDescriptor descriptor = ZKCommandRegistry.find(args).orElse(null);
        if (descriptor == null) {
            terminalResult.setErrMsg("Command not found: " + commandName);
            return terminalResult;
        }
        try {
            this.disable();
            ZooKeeper zooKeeper = null;
            if (descriptor.type() == ZKCommandDescriptor.Type.CLI
                    && !CLI_COMMANDS_WITHOUT_CONNECTION.contains(commandName.toLowerCase(Locale.ROOT))) {
                zooKeeper = this.zooKeeper();
            }
            ShellConnect connect = this.shellConnect();
            String host = connect == null ? "" : connect.hostIp();
            int port = connect == null ? 0 : connect.hostPort();
            terminalResult.setResult(this.commandExecutor.execute(descriptor, args, zooKeeper, host, port));
        } catch (Exception ex) {
            terminalResult.setException(ex);
        } finally {
            this.enable();
        }
        return terminalResult;
    }

    @Override
    public void fontSizeIncr() {
        super.fontSizeIncr();
        this.saveFontSize();
    }

    @Override
    public void fontSizeDecr() {
        super.fontSizeDecr();
        this.saveFontSize();
    }

    /**
     * 保存当前终端字号设置。
     */
    private void saveFontSize() {
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setTerminalFontSize((byte) this.getFontSize());
        ShellSettingStore.INSTANCE.replace(setting);
    }

    private boolean initPrompts;

    @Override
    public Set<String> getPrompts() {
        if (!this.initPrompts) {
            this.initPrompts = true;
            Set<String> prompts = super.getPrompts();
            if (prompts == null) {
                prompts = new HashSet<>();
            } else {
                prompts = new HashSet<>(prompts);
            }
            // 设置内容提示符
            prompts.addAll(ZKCommandRegistry.commandNames());
            this.setPrompts(prompts);
        }
        return super.getPrompts();
    }

    @Override
    public void initNode() {
        this.keyHandler(ZKTerminalKeyHandler.INSTANCE);
        this.helpHandler(ZKTerminalHelpHandler.INSTANCE);
        this.mouseHandler(ZKTerminalMouseHandler.INSTANCE);
        this.historyHandler(ShellTerminalHistoryHandler.INSTANCE);
        this.completeHandler(ZKTerminalCompleteHandler.INSTANCE);
        super.initNode();
    }

    @Override
    public void destroy() {
        if (this.client != null && this.stateChangeListener != null) {
            this.client.stateProperty().removeListener(this.stateChangeListener);
        }
        this.stateChangeListener = null;
        super.destroy();
    }
}
