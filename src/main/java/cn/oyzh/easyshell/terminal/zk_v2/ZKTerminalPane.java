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

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 基于动态命令发现的 ZooKeeper v2 终端。
 */
public class ZKTerminalPane extends TerminalPane {

    public static final String TERMINAL_NAME = "zookeeper-v2";

    private static final Set<String> CLI_COMMANDS_WITHOUT_CONNECTION = Set.of("version");

    private final ZKCommandExecutor commandExecutor = new ZKCommandExecutor();

    private ShellZKClient client;
    private ShellZKConnectInfo connectInfo;
    private ChangeListener<ShellConnState> stateChangeListener;

    @Override
    public Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.terminalFontConfig());
    }

    public ShellZKClient getClient() {
        return client;
    }

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

    public boolean isTemporary() {
        return this.client == null || this.client.iid() == null;
    }

    @Override
    public void outputPrompt() {
        if (!this.isConnecting()) {
            super.outputPrompt();
        }
    }

    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    public boolean isConnecting() {
        return this.client != null && this.client.isConnecting();
    }

    public boolean isClosed() {
        return this.client != null && this.client.isClosed();
    }

    public void connect(String input) {
        this.connectInfo = ShellZKConnectUtil.parse(input);
        if (this.connectInfo != null) {
            this.disable();
            ShellZKConnectUtil.copyConnect(this.connectInfo, this.shellConnect());
            this.start();
        }
    }

    private void initByTemporary() {
        this.outputLine("connect [-timeout timeout] [-server server] [-r]");
        this.outputLine("-timeout " + I18nResourceBundle.i18nString("base.unit", "base.ms"));
        this.outputLine("-server ip:" + I18nHelper.port());
        this.outputLine("-r " + I18nHelper.readonlyMode());
        this.appendByPrompt("connect -timeout 3000 -server localhost:2181");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    private void initByPermanent() {
        this.flushPrompt();
        this.appendByPrompt("");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

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

    public TerminalExecuteResult executeCommand(List<String> args) {
        TerminalExecuteResult terminalResult = TerminalExecuteResult.ok();
        if (args == null || args.isEmpty()) {
            terminalResult.setErrMsg("No command entered");
            return terminalResult;
        }
        String commandName = args.getFirst();
        ZKCommandDescriptor descriptor = ZKCommandRegistry.find(commandName).orElse(null);
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

    private void saveFontSize() {
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setTerminalFontSize((byte) this.getFontSize());
        ShellSettingStore.INSTANCE.replace(setting);
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
