package cn.oyzh.easyshell.terminal.dameng;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.query.dameng.DamengExecuteResult;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
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

import java.util.List;

/**
 * dameng终端文本域
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class DamengTerminalPane extends TerminalPane {

    @Override
    protected Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.terminalFontConfig());
    }

    /**
     * dameng客户端
     */
    private ShellDamengClient client;

    public ShellDamengClient getClient() {
        return client;
    }

    public ShellConnect shellConnect() {
        return this.client == null ? null : this.client.getShellConnect();
    }

    // /**
    //  * 客户端连接状态监听器
    //  */
    // private ChangeListener<DBConnState> stateChangeListener;

    @Override
    public void flushPrompt() {
        String str = this.dbName;
        str += "@" + this.shellConnect().getName();
        if (this.isConnecting()) {
            str += "(" + I18nHelper.connectIng() + ")> ";
        } else if (this.isConnected()) {
            str += "(" + I18nHelper.connected() + ")> ";
        } else {
            str += "> ";
        }
        this.prompt(str);
    }

    public static final String TERMINAL_NAME = "dameng";

    @Override
    public String terminalName() {
        return TERMINAL_NAME;
    }

    private String dbName;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
        this.flushPrompt();
    }

    /**
     * 初始化
     *
     * @param client 客户端
     */
    public void init(ShellDamengClient client, String dbName) {
        this.client = client;
        this.setDbName(dbName);
        FXUtil.runPulse(() -> {
            this.disableInput();
            this.outputLine(ShellI18nHelper.welcome());
            this.outputLine("Powered By oyzh(2024-2026).");
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

    public TerminalExecuteResult eval(String input) {
        TerminalExecuteResult terminalResult = new TerminalExecuteResult();
        try {
            if (this.dbName == null) {
                terminalResult.setResult("No database selected. Use 'use <database>' to select one.");
                return terminalResult;
            }
            DBQueryResults<DamengExecuteResult> results = this.client.executeSql(this.dbName, input);
            if (!results.isSuccess()) {
                terminalResult.setException(new RuntimeException(results.getErrMsg()));
            } else if (results.isEmpty()) {
                terminalResult.setResult("OK");
            } else {
                StringBuilder sb = new StringBuilder();
                for (DamengExecuteResult result : results.getResults()) {
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
    private String formatResultSet(DamengExecuteResult result) {
        StringBuilder sb = new StringBuilder();
        DamengColumns columns = result.getColumns();
        List<DamengRecord> records = result.getRecords();
        if (columns == null || records == null) {
            return "";
        }

        int colCount = columns.size();
        int[] colWidths = new int[colCount];
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = Math.max(colWidths[i], columns.get(i).getName().length());
        }
        for (DamengRecord record : records) {
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
        for (DamengRecord record : records) {
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
        return sb.append("#").toString();
    }

    @Override
    protected TerminalCommandHandler findHandler(String input) {
        TerminalCommandHandler<?, ?> handler = TerminalManager.findHandler(DamengTerminalPane.TERMINAL_NAME, input);
        if (handler == null) {
            handler = new DamengTerminalCommandHandler<>() {

                @Override
                public String commandName() {
                    return "";
                }

                @Override
                public TerminalExecuteResult execute(TerminalCommand command, DamengTerminalPane terminal) {
                    String input = command.getContent();
                    return terminal.eval(input);
                }
            };
        }
        return handler;
    }

    @Override
    public void initNode() {
        this.keyHandler(DamengTerminalKeyHandler.INSTANCE);
        this.helpHandler(DamengTerminalHelpHandler.INSTANCE);
        this.mouseHandler(DamengTerminalMouseHandler.INSTANCE);
        this.historyHandler(ShellTerminalHistoryHandler.INSTANCE);
        this.completeHandler(DamengTerminalCompleteHandler.INSTANCE);
        super.initNode();
    }
}
