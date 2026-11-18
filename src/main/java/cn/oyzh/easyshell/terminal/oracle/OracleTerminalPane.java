package cn.oyzh.easyshell.terminal.oracle;

import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.query.oracle.OracleExecuteResult;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.query.oracle.ShellOracleQueryUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.terminal.ShellTerminalHistoryHandler;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.db.query.DBQueryResults;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.terminal.TerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.util.TerminalManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.text.Font;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Oracle终端文本域
 *
 * @author oyzh
 * @since 2026-06-16
 */
public class OracleTerminalPane extends TerminalPane {

    @Override
    public Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.terminalFontConfig());
    }

    /**
     * Oracle客户端
     */
    private ShellOracleClient client;

    /**
     * 获取Oracle客户端
     *
     * @return Oracle客户端
     */
    public ShellOracleClient getClient() {
        return client;
    }

    /**
     * 获取关联的连接信息
     *
     * @return 连接信息，客户端为空时返回 null
     */
    public ShellConnect shellConnect() {
        return this.client == null ? null : this.client.getShellConnect();
    }

    // /**
    //  * 客户端连接状态监听器
    //  */
    // private ChangeListener<DBConnState> stateChangeListener;

    @Override
    public void flushPrompt() {
        String str = this.schema;
        str += "@" + this.shellConnect().getName();
        if (this.isConnecting()) {
            str += ShellI18nHelper.terminalConnectingPrompt();
        } else if (this.isConnected()) {
            str += ShellI18nHelper.terminalConnectedPrompt();
        } else {
            str += "> ";
        }
        this.prompt(str);
    }

    /**
     * 终端名称
     */
    public static final String TERMINAL_NAME = "oracle";

    static {
        TerminalManager.setLoadHandler(TERMINAL_NAME, OracleTerminalManager::registerHandlers);
    }

    @Override
    public String terminalName() {
        return TERMINAL_NAME;
    }

    /**
     * 数据库名称
     */
    private String schema;

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 设置数据库名称并刷新提示符
     *
     * @param schema 数据库名称
     */
    public void setSchema(String schema) {
        this.schema = schema;
        this.flushPrompt();
    }

    /**
     * 初始化
     *
     * @param client 客户端
     * @param schema 数据库名称
     */
    public void init(ShellOracleClient client, String schema) {
        this.client = client;
        this.setSchema(schema);
        FXUtil.runPulse(() -> {
            this.disableInput();
            this.outputLine(ShellI18nHelper.welcome());
            this.outputLine("Powered By oyzh(2026-2026).");
            this.flushPrompt();
            if (this.isTemporary()) {
                this.initByTemporary();
            } else {
                this.initByPermanent();
            }
        });
    }


    /**
     * 是否临时连接
     *
     * @return 结果
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
     * 临时连接处理
     */
    private void initByTemporary() {
        this.outputLine("Please enter connection info or SQL.");
        this.appendByPrompt("");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    /**
     * 常驻连接处理
     */
    private void initByPermanent() {
        this.flushPrompt();
        this.appendByPrompt("");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    // /**
    //  * 刷新光标并移动到尾部
    //  */
    // private void flushAndMoveCaretEnd() {
    //     ExecutorUtil.start(() -> {
    //         this.flushCaret();
    //         this.moveCaretEnd();
    //     }, 50);
    // }

    // /**
    //  * 初始化连接状态监听器
    //  */
    // private void initStatListener() {
    //     if (this.stateChangeListener == null) {
    //         this.stateChangeListener = (observableValue, state, t1) -> {
    //             this.flushPrompt();
    //             String host = this.dbConnect != null ? this.dbConnect.getHost() : "";
    //             if (t1 == DBConnState.CONNECTED) {
    //                 this.outputLine(host + " " + I18nHelper.connectSuccess() + ".");
    //                 this.outputPrompt();
    //                 this.flushCaret();
    //                 super.enableInput();
    //             } else if (t1 == DBConnState.CLOSED) {
    //                 this.outputLine(host + " " + I18nHelper.connectionClosed() + ".");
    //                 this.enableInput();
    //             } else if (t1 == DBConnState.CONNECTING) {
    //                 this.outputLine(host + " " + I18nHelper.connectIng() + "...", false);
    //             } else if (t1 == DBConnState.FAILED) {
    //                 this.outputLine(host + " " + I18nHelper.connectFail() + ".");
    //                 this.enableInput();
    //             }
    //             JulLog.info("connState={}", t1);
    //         };
    //         this.getClient().addStateListener(this.stateChangeListener);
    //     }
    // }

    @Override
    public void enableInput() {
        if (this.isConnecting()) {
            return;
        }
        if (this.isConnected() || (!this.isConnected() && this.isTemporary())) {
            super.enableInput();
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
     * 保存字体大小
     */
    private void saveFontSize() {
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setTerminalFontSize((byte) this.getFontSize());
        ShellSettingStore.INSTANCE.replace(setting);
    }

    // @Override
    // public void destroy() {
    //     if (this.client != null && this.stateChangeListener != null) {
    //         this.client.stateProperty().removeListener(this.stateChangeListener);
    //     }
    //     this.stateChangeListener = null;
    //     super.destroy();
    // }

    /**
     * 执行输入的终端命令
     *
     * @param input 输入内容
     * @return 执行结果
     */
    public TerminalExecuteResult eval(String input) {
        TerminalExecuteResult terminalResult = new TerminalExecuteResult();
        try {
            if (this.schema == null) {
                terminalResult.setResult("No schema selected. Use 'use <schema>' to select one.");
                return terminalResult;
            }
            DBQueryResults<OracleExecuteResult> results = this.client.executeSql(this.schema, input);
            if (!results.isSuccess()) {
                terminalResult.setException(new RuntimeException(results.getErrMsg()));
            } else if (results.isEmpty()) {
                terminalResult.setResult("OK");
            } else {
                StringBuilder sb = new StringBuilder();
                for (OracleExecuteResult result : results.getResults()) {
                    if (result.isSuccess()) {
                        if (result.getUpdateCount() > 0) {
                            sb.append("Query OK, ").append(result.getUpdateCount()).append(" rows affected");
                            long ms = result.getUsedMs();
                            if (ms > 0) {
                                sb.append(" (").append(ms).append(" ms)");
                            }
                            sb.append(this.lineEndingText());
                        } else if (result.getRecords() != null && !result.getRecords().isEmpty()) {
                            sb.append(this.formatResultSet(result));
                        } else {
                            sb.append("OK").append(this.lineEndingText());
                        }
                    } else {
                        sb.append("ERROR: ").append(result.getMsg()).append(this.lineEndingText());
                    }
                }
                terminalResult.setResult(sb.toString().trim());
            }
        } catch (Exception ex) {
            terminalResult.setException(ex);
        }
        return terminalResult;
    }

    /**
     * 格式化结果集
     *
     * @param result 结果集
     * @return 结果
     */
    private String formatResultSet(OracleExecuteResult result) {
        StringBuilder sb = new StringBuilder();
        OracleColumns columns = result.getColumns();
        List<OracleRecord> records = result.getRecords();
        if (columns == null || records == null) {
            return "";
        }

        int colCount = columns.size();
        int[] colWidths = new int[colCount];
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = Math.max(colWidths[i], columns.get(i).getName().length());
        }
        for (OracleRecord record : records) {
            for (int i = 0; i < colCount; i++) {
                Object val = record.getValue(columns.get(i).getName());
                String str = val == null ? "NULL" : val.toString();
                colWidths[i] = Math.max(colWidths[i], str.length());
            }
        }

        // header
        for (int i = 0; i < colCount; i++) {
            sb.append(String.format("%-" + (colWidths[i] + 2) + "s", columns.get(i).getName()));
        }
        sb.append(this.lineEndingText());

        // separator
        for (int i = 0; i < colCount; i++) {
            sb.append("-".repeat(colWidths[i]));
            sb.append("  ");
        }
        sb.append(this.lineEndingText());

        // data rows
        for (OracleRecord record : records) {
            for (int i = 0; i < colCount; i++) {
                Object val = record.getValue(columns.get(i).getName());
                String str = val == null ? "NULL" : val.toString();
                sb.append(String.format("%-" + (colWidths[i] + 2) + "s", str));
            }
            sb.append(this.lineEndingText());
        }

        sb.append("#").append(records.size()).append(" row(s) in set");

        long ms = result.getUsedMs();
        if (ms > 0) {
            sb.append(" (").append(ms).append(" ms)");
        }
        return sb.toString();
    }

    @Override
    protected TerminalCommandHandler findHandler(String input) {
        TerminalCommandHandler<?, ?> handler = TerminalManager.findHandler(OracleTerminalPane.TERMINAL_NAME, input);
        if (handler == null) {
            handler = new OracleTerminalCommandHandler<>() {

                @Override
                public String commandName() {
                    return "";
                }

                @Override
                public TerminalExecuteResult execute(TerminalCommand command, OracleTerminalPane terminal) {
                    String input = command.getContent();
                    return terminal.eval(input);
                }
            };
        }
        return handler;
    }

    @Override
    public void initNode() {
        this.keyHandler(OracleTerminalKeyHandler.INSTANCE);
        this.helpHandler(OracleTerminalHelpHandler.INSTANCE);
        this.mouseHandler(OracleTerminalMouseHandler.INSTANCE);
        this.historyHandler(ShellTerminalHistoryHandler.INSTANCE);
        this.completeHandler(OracleTerminalCompleteHandler.INSTANCE);
        super.initNode();
    }

    /**
     * 是否已初始化提示词
     */
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
            prompts.addAll(ShellOracleQueryUtil.getDbKeywords());
            this.setPrompts(prompts);
        }
        return super.getPrompts();
    }

}
