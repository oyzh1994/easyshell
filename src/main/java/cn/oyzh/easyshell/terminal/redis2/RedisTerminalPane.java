package cn.oyzh.easyshell.terminal.redis2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.dto.redis.ShellRedisConnectInfo;
import cn.oyzh.easyshell.exception.ShellExceptionParser;
import cn.oyzh.easyshell.exception.ShellReadonlyOperationException;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisCommandSession;
import cn.oyzh.easyshell.redis.ShellRedisConnectUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.terminal.ShellTerminalHistoryHandler;
import cn.oyzh.easyshell.util.ShellI18nHelper;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 基于 Redis 原生命令动态执行的 v2 终端。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class RedisTerminalPane extends TerminalPane {

    /**
     * Redis v2 终端名称。
     */
    public static final String TERMINAL_NAME = "redis-v2";

    /**
     * Redis 原生命令执行器。
     */
    private final RedisCommandExecutor commandExecutor = new RedisCommandExecutor();
    /**
     * Redis 客户端。
     */
    private ShellRedisClient client;
    /**
     * 临时连接解析结果。
     */
    private ShellRedisConnectInfo connectInfo;
    /**
     * 客户端连接状态监听器。
     */
    private ChangeListener<ShellConnState> stateChangeListener;
    /**
     * 当前 Redis 命令会话。
     */
    private ShellRedisCommandSession commandSession;
    /**
     * 当前连接的动态命令注册表。
     */
    private RedisCommandRegistry registry;

    /**
     * 当前 Redis 数据库编号。
     */
    private Integer dbIndex;

    @Override
    public Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.terminalFontConfig());
    }

    /**
     * 获取 Redis 客户端。
     *
     * @return Redis 客户端
     */
    public ShellRedisClient getClient() {
        return client;
    }

    /**
     * 获取当前 Redis 数据库编号。
     *
     * @return 数据库编号
     */
    public Integer getDbIndex() {
        return dbIndex;
    }

    /**
     * 获取当前连接的命令注册表，首次调用时从服务端加载。
     *
     * @return Redis 命令注册表
     */
    public RedisCommandRegistry registry() {
        if (this.registry == null) {
            try {
                this.registry = RedisCommandRegistry.load(this.client);
                this.getPrompts();
            } catch (Exception ex) {
                JulLog.warn("load redis command registry error", ex);
                this.registry = RedisCommandRegistry.from(Map.of());
            }
        }
        return this.registry;
    }

    @Override
    public void flushPrompt() {
        String prompt;
        if (this.isTemporary()) {
            prompt = "redis " + I18nHelper.connection();
        } else {
            prompt = this.client.connectName();
        }
        ShellConnect connect = this.shellConnect();
        if (connect != null && connect.getHost() != null) {
            prompt += "@" + connect.getHost();
        }
        String db = this.dbIndex == null ? "" : "@db" + this.dbIndex;
        if (this.isConnecting()) {
            prompt += "(" + I18nHelper.connectIng() + db + ")> ";
        } else if (this.isConnected()) {
            prompt += "(" + (this.client.isReadonly() ? I18nHelper.readonlyMode() : I18nHelper.connected()) + db + ")> ";
        } else {
            prompt += db + "> ";
        }
        this.prompt(prompt);
    }

    @Override
    public String terminalName() {
        return TERMINAL_NAME;
    }

    /**
     * 初始化 Redis 终端。
     *
     * @param client  Redis 客户端
     * @param dbIndex 初始数据库编号
     */
    public void init(ShellRedisClient client, Integer dbIndex) {
        this.client = client;
        this.dbIndex = dbIndex == null ? 0 : dbIndex;
        FXUtil.runPulse(() -> {
            this.disableInput();
            this.outputLine(ShellI18nHelper.welcome());
            this.outputLine("Powered By oyzh(2023-2026).");
            this.flushPrompt();
            if (this.isTemporary()) {
                this.initByTemporary();
            } else {
                this.initByPermanent();
            }
        });
        this.registry();
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
     * 判断 Redis 是否已连接。
     *
     * @return 是否已连接
     */
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    /**
     * 判断 Redis 是否正在连接。
     *
     * @return 是否正在连接
     */
    public boolean isConnecting() {
        return this.client != null && this.client.isConnecting();
    }

    /**
     * 判断 Redis 连接是否已关闭。
     *
     * @return 是否已关闭
     */
    public boolean isClosed() {
        return this.client != null && this.client.isClosed();
    }

    /**
     * 初始化临时终端的连接提示。
     */
    private void initByTemporary() {
        this.outputLine("connect [-timeout timeout] [-h host] [-p port] [-u user] [-a password] [-n db] [-r]");
        this.outputLine("-timeout " + I18nResourceBundle.i18nString("base.unit", "base.ms"));
        this.outputLine("-h " + I18nHelper.host());
        this.outputLine("-p " + I18nHelper.port());
        this.outputLine("-u " + I18nHelper.userName());
        this.outputLine("-a " + I18nHelper.password());
        this.outputLine("-n " + I18nHelper.database());
        this.outputLine("-r " + I18nHelper.readonlyMode());
        this.appendByPrompt("connect -timeout 3000 -h 127.0.0.1 -p 6379 -n 0");
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
     * 按终端输入解析并建立临时 Redis 连接。
     *
     * @param input connect 命令内容
     */
    public void connect(String input) {
        this.connectInfo = ShellRedisConnectUtil.parse(input);
        if (this.connectInfo != null) {
            this.disable();
            ShellRedisConnectUtil.copyConnect(this.connectInfo, this.shellConnect());
            this.dbIndex = this.connectInfo.getDb();
            this.start(this.dbIndex);
        }
    }

    /**
     * 异步启动 Redis 客户端。
     *
     * @param db 初始数据库编号
     */
    private void start(int db) {
        TaskManager.startSync(() -> {
            try {
                this.initStateListener();
                this.client.startDatabase(db);
            } catch (Exception ex) {
                this.onError(ShellExceptionParser.INSTANCE.apply(ex));
            } finally {
                this.enable();
            }
        });
    }

    /**
     * 注册 Redis 连接状态监听器。
     */
    private void initStateListener() {
        if (this.stateChangeListener != null) {
            return;
        }
        this.stateChangeListener = (observable, oldState, newState) -> {
            this.closeCommandSession();
            this.flushPrompt();
            ShellConnect connect = this.shellConnect();
            String host = connect == null ? "" : connect.getHost();
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
                this.outputLine(host + " " + I18nHelper.connectionBroken() + " .");
                this.enableInput();
            } else if (newState == ShellConnState.FAILED) {
                this.outputLine(host + " " + I18nHelper.connectFail() + " .");
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
        return this.client == null ? null : this.client.shellConnect();
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
        List<String> words = RedisCommandTokenizer.tokenize(input);
        String commandName = words.isEmpty() ? "" : words.getFirst();
        RedisCommandDescriptor descriptor = words.isEmpty() ? null : this.registry().find(words);
        return descriptor == null ? new RedisDynamicCommandHandler(commandName) : new RedisDynamicCommandHandler(descriptor);
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
     * 执行完整 Redis 命令参数。
     *
     * @param args 完整命令参数
     * @return 执行结果
     */
    public TerminalExecuteResult executeCommand(List<String> args) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        if (args == null || args.isEmpty() || StringUtil.isBlank(args.getFirst())) {
            result.setErrMsg("No command entered");
            return result;
        }
        if (!this.isConnected()) {
            result.setErrMsg("Redis is not connected");
            return result;
        }
        RedisCommandDescriptor descriptor = this.registry().find(args);
        if (this.client.isReadonly() && isWriteCommand(descriptor)) {
            return TerminalExecuteResult.fail(new ShellReadonlyOperationException());
        }
        try {
            this.disable();
            if (this.commandSession == null) {
                this.commandSession = this.client.openCommandSession(this.dbIndex);
            }
            Object response = this.commandExecutor.execute(args, descriptor, this.commandSession);
            if (args.getFirst().equalsIgnoreCase("select") && args.size() == 2) {
                this.dbIndex = Integer.parseInt(args.get(1));
                this.flushPrompt();
            }
            result.setResult(RedisResponseFormatter.format(response, this.lineEndingText()));
        } catch (Exception ex) {
            this.closeCommandSession();
            result.setException(ex);
        } finally {
            this.enable();
        }
        return result;
    }

    /**
     * 判断命令元数据是否标记为写命令。
     */
    private boolean isWriteCommand(RedisCommandDescriptor descriptor) {
        return descriptor != null && descriptor.flags().stream().anyMatch(flag -> flag.equalsIgnoreCase("write"));
    }

    /**
     * 关闭当前命令会话并清理状态。
     */
    private void closeCommandSession() {
        if (this.commandSession != null) {
            try {
                this.commandSession.close();
            } catch (Exception ex) {
                JulLog.warn("close redis command session error", ex);
            }
            this.commandSession = null;
        }
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

    @Override
    public Set<String> getPrompts() {
        if (this.client != null && this.registry != null) {
            Set<String> prompts = super.getPrompts();
            if (prompts == null) {
                prompts = new HashSet<>();
            } else {
                prompts = new HashSet<>(prompts);
            }
            // 设置内容提示符
            prompts.addAll(this.registry.commandNames());
            this.setPrompts(prompts);
        }
        return super.getPrompts();
    }

    @Override
    public void initNode() {
        this.keyHandler(RedisTerminalKeyHandler.INSTANCE);
        this.helpHandler(RedisTerminalHelpHandler.INSTANCE);
        this.mouseHandler(RedisTerminalMouseHandler.INSTANCE);
        this.historyHandler(ShellTerminalHistoryHandler.INSTANCE);
        this.completeHandler(RedisTerminalCompleteHandler.INSTANCE);
        super.initNode();
    }

    @Override
    public void destroy() {
        this.closeCommandSession();
        if (this.client != null && this.stateChangeListener != null) {
            this.client.stateProperty().removeListener(this.stateChangeListener);
        }
        this.stateChangeListener = null;
        super.destroy();
    }
}
