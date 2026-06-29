package cn.oyzh.easyshell.terminal.mongo;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.dto.mongo.ShellMongoConnectInfo;
import cn.oyzh.easyshell.exception.mongo.MongoExceptionParser;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.mongo.MongoI18nHelper;
import cn.oyzh.easyshell.util.mongo.ShellMongoConnectUtil;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.TerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.util.TerminalManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.scene.text.Font;

/**
 * zk终端文本域
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class MongoTerminalPane extends TerminalPane {

    @Override
    protected Font getEditorFont() {
        if (super.getEditorFont() == null) {
            ShellSetting setting = ShellSettingStore.SETTING;
            Font font = FontManager.toFont(setting.terminalFontConfig());
            super.setEditorFont(font);
        }
        return super.getEditorFont();
    }

    /**
     * zk客户端
     */
    private ShellMongoClient client;

    public ShellMongoClient getClient() {
        return client;
    }

    /**
     * zk连接
     */
    private ShellMongoConnectInfo connectInfo;

    /**
     * 客户端连接状态监听器
     */
    private ChangeListener<ShellConnState> stateChangeListener;

    @Override
    public void flushPrompt() {
        String str;
        if (this.isTemporary()) {
            str = "mongo" + I18nHelper.connect();
        } else {
            str = this.client.connectName();
        }
        if (this.shellConnect().getHost() != null) {
            str += "@" + this.shellConnect().getHost();
        }
        if (this.isConnecting()) {
            str += "(" + I18nHelper.connectIng() + ")> ";
        } else if (this.isConnected()) {
            str += "(" + I18nHelper.connected() + ")> ";
        } else {
            str += "> ";
        }
        this.prompt(str);
    }

    public static final String TERMINAL_NAME = "zookeeper";

    @Override
    public String terminalName() {
        return TERMINAL_NAME;
    }

    private String dbName;

    /**
     * 初始化
     *
     * @param client 客户端
     */
    public void init(ShellMongoClient client, String dbName) {
        this.client = client;
        this.dbName = dbName;
        this.disableInput();
        this.outputLine(MongoI18nHelper.welcome());
        this.outputLine("Powered By oyzh(2026-2026).");
        this.flushPrompt();
        if (this.isTemporary()) {
            this.initByTemporary();
        } else {
            this.initByPermanent();
        }
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
        this.client.shellEngine().db(dbName);
    }

    /**
     * 是否临时连接
     *
     * @return 结果
     */
    public boolean isTemporary() {
        return this.client.iid() == null;
    }

    @Override
    public void outputPrompt() {
        if (!this.client.isConnecting()) {
            super.outputPrompt();
        }
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
    public void connect(String input) {
        this.connectInfo = ShellMongoConnectUtil.parse(input);
        if (this.connectInfo != null) {
            this.disable();
            ShellMongoConnectUtil.copyConnect(this.connectInfo, this.shellConnect());
            this.start();
        }
    }

    /**
     * 临时连接处理
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
     * 常驻连接处理
     */
    private void initByPermanent() {
        //        this.start();
        this.flushPrompt();
        this.appendByPrompt("");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    /**
     * 开始连接
     */
    private void start() {
        TaskManager.startSync(() -> {
            try {
                this.initStatListener();
                this.client.start();
            } catch (Throwable ex) {
                this.onError(MongoExceptionParser.INSTANCE.apply(ex));
            } finally {
                this.enable();
            }
        });
    }

    /**
     * 刷新光标并移动到尾部
     */
    private void flushAndMoveCaretEnd() {
        ExecutorUtil.start(() -> {
            this.flushCaret();
            this.moveCaretEnd();
        }, 50);
    }

    /**
     * 初始化连接状态监听器
     */
    private void initStatListener() {
        if (this.stateChangeListener == null) {
            this.stateChangeListener = (observableValue, state, t1) -> {
                this.flushPrompt();
                // 获取连接
                String host = this.shellConnect().getHost();
                if (t1 == ShellConnState.CONNECTED) {
                    this.outputLine(host + I18nHelper.connectSuccess() + " .");
                    this.outputLine(I18nHelper.terminalTip2());
                    this.outputLine(I18nHelper.terminalTip1());
                    this.outputPrompt();
                    this.flushCaret();
                    super.enableInput();
                } else if (t1 == ShellConnState.CLOSED) {
                    this.outputLine(host + " " + I18nHelper.connectionClosed() + " .");
                    this.enableInput();
                } else if (t1 == ShellConnState.CONNECTING) {
                    this.outputLine(host + " " + I18nHelper.connectionConnecting() + " .", false);
                } else if (t1 == ShellConnState.INTERRUPTED) {
                    this.outputLine(host + " " + I18nHelper.connectSuspended() + " .");
                    this.enableInput();
                } else if (t1 == ShellConnState.RECONNECTED) {
                    this.outputLine(host + " " + I18nHelper.connectReconnected() + " .");
                    this.outputPrompt();
                    this.flushCaret();
                    super.enableInput();
                } else if (t1 == ShellConnState.FAILED) {
                    this.outputLine(host + I18nHelper.connectFail() + " .");
                    if (this.connectInfo != null) {
                        this.appendByPrompt(this.connectInfo.getInput());
                    }
                    this.flushAndMoveCaretEnd();
                    this.enableInput();
                }
                JulLog.info("connState={}", t1);
            };
            this.getClient().addStateListener(this.stateChangeListener);
        }
    }

    @Override
    public void enableInput() {
        if (this.isConnecting()) {
            return;
        }
        if (this.isConnected() || (!this.isConnected() && this.isTemporary())) {
            super.enableInput();
        }
    }

    public ShellConnect shellConnect() {
        return this.getClient().getShellConnect();
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
     * 保存字体大小
     */
    private void saveFontSize() {
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setTerminalFontSize((byte) this.getFontSize());
        ShellSettingStore.INSTANCE.replace(setting);
    }

    @Override
    public void destroy() {
        if (this.client != null) {
            this.client.stateProperty().unbind();
        }
        this.stateChangeListener = null;
        super.destroy();
    }

    public TerminalExecuteResult eval(String input) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        try {
            Object o = this.client.eval(this.dbName, input);
            result.setResult(o);
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }

    @Override
    protected TerminalCommandHandler findHandler(String input) {
        // 特殊命令
        TerminalCommandHandler<?, ?> handler = TerminalManager.findHandler(MongoTerminalPane.TERMINAL_NAME, input);
        if (handler == null) {
            handler = new MongoTerminalCommandHandler<>() {

                @Override
                public String commandName() {
                    return "";
                }

                @Override
                public TerminalExecuteResult execute(TerminalCommand command, MongoTerminalPane terminal) {
                    String input = command.getCommand();
                    return terminal.eval(input);
                }
            };
        }
        return handler;
    }

    @Override
    public void initNode() {
        this.keyHandler(MongoTerminalKeyHandler.INSTANCE);
        this.helpHandler(MongoTerminalHelpHandler.INSTANCE);
        this.mouseHandler(MongoTerminalMouseHandler.INSTANCE);
        this.historyHandler(MongoTerminalHistoryHandler.INSTANCE);
        this.completeHandler(MongoTerminalCompleteHandler.INSTANCE);
        super.initNode();
    }
}
