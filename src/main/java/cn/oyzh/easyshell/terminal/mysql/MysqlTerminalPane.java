package cn.oyzh.easyshell.terminal.mysql;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.query.MysqlExecuteResult;
import cn.oyzh.easyshell.mysql.query.MysqlQueryResults;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.terminal.TerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.util.TerminalManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.scene.text.Font;

import java.util.List;

/**
 * mysql终端文本域
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class MysqlTerminalPane extends TerminalPane {

    @Override
    protected Font getEditorFont() {
        if (super.getEditorFont() == null) {
            ShellSetting setting = ShellSettingStore.SETTING;
            Font font = FontManager.toFont(setting.terminalFontConfig());
            super.setEditorFont(font);
        }
        return super.getEditorFont();
    }

    private ShellMysqlClient client;

    public ShellMysqlClient getClient() {
        return client;
    }

    private ShellConnect shellConnect;

    private ChangeListener<ShellConnState> stateChangeListener;

    @Override
    public void flushPrompt() {
        String str;
        if (this.isTemporary()) {
            str = "mysql " + I18nHelper.connection();
        } else {
            str = this.client.connectName();
        }
        if (this.shellConnect != null && this.shellConnect.getHost() != null) {
            str += "@" + this.shellConnect.getHost();
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

    public static final String TERMINAL_NAME = "mysql";

    @Override
    public String terminalName() {
        return TERMINAL_NAME;
    }

    private String dbName;

    public void init(ShellMysqlClient client, String dbName) {
        this.client = client;
        this.shellConnect = client != null ? client.getShellConnect() : null;
        this.dbName = dbName;
        this.disableInput();
        this.outputLine(ShellI18nHelper.welcome());
        this.outputLine("Powered By oyzh(2024-2026).");
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
    }

    public boolean isTemporary() {
        return this.client == null || this.client.getShellConnect() == null || this.client.getShellConnect().getId() == null;
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

    private void initByTemporary() {
        this.outputLine("Please enter connection info or SQL.");
        this.appendByPrompt("");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    private void initByPermanent() {
        this.flushPrompt();
        this.appendByPrompt("");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    private void flushAndMoveCaretEnd() {
        ExecutorUtil.start(() -> {
            this.flushCaret();
            this.moveCaretEnd();
        }, 50);
    }

    private void initStatListener() {
        if (this.stateChangeListener == null) {
            this.stateChangeListener = (observableValue, state, t1) -> {
                this.flushPrompt();
                String host = this.shellConnect != null ? this.shellConnect.getHost() : "";
                if (t1 == ShellConnState.CONNECTED) {
                    this.outputLine(host + " " + I18nHelper.connectSuccess() + ".");
                    this.outputPrompt();
                    this.flushCaret();
                    super.enableInput();
                } else if (t1 == ShellConnState.CLOSED) {
                    this.outputLine(host + " " + I18nHelper.connectionClosed() + ".");
                    this.enableInput();
                } else if (t1 == ShellConnState.CONNECTING) {
                    this.outputLine(host + " " + I18nHelper.connectIng() + "...", false);
                } else if (t1 == ShellConnState.FAILED) {
                    this.outputLine(host + " " + I18nHelper.connectFail() + ".");
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
    public void destroy() {
        if (this.client != null && this.stateChangeListener != null) {
            this.client.stateProperty().removeListener(this.stateChangeListener);
        }
        this.stateChangeListener = null;
        super.destroy();
    }

    public TerminalExecuteResult eval(String input) {
        TerminalExecuteResult terminalResult = new TerminalExecuteResult();
        try {
            if (this.dbName == null) {
                terminalResult.setResult("No database selected. Use 'use <database>' to select one.");
                return terminalResult;
            }
            MysqlQueryResults<MysqlExecuteResult> results = this.client.executeSql(this.dbName, input);
            if (!results.isSuccess()) {
                terminalResult.setException(new RuntimeException(results.getErrMsg()));
            } else if (results.isEmpty()) {
                terminalResult.setResult("OK");
            } else {
                StringBuilder sb = new StringBuilder();
                for (MysqlExecuteResult result : results.getResults()) {
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

    private String formatResultSet(MysqlExecuteResult result) {
        StringBuilder sb = new StringBuilder();
        MysqlColumns columns = result.getColumns();
        List<MysqlRecord> records = result.getRecords();
        if (columns == null || records == null) {
            return "";
        }

        int colCount = columns.size();
        int[] colWidths = new int[colCount];
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = Math.max(colWidths[i], columns.get(i).getName().length());
        }
        for (MysqlRecord record : records) {
            for (int i = 0; i < colCount; i++) {
                Object val = record.getValue(columns.get(i).getName());
                String str = val == null ? "NULL" : val.toString();
                colWidths[i] = Math.max(colWidths[i], str.length());
            }
        }

        for (int i = 0; i < colCount; i++) {
            sb.append(String.format("%-" + (colWidths[i] + 2) + "s", columns.get(i).getName()));
        }
        sb.append(this.lineEndingText());

        for (int i = 0; i < colCount; i++) {
            sb.append("-".repeat(colWidths[i]));
            sb.append("  ");
        }
        sb.append(this.lineEndingText());

        for (MysqlRecord record : records) {
            for (int i = 0; i < colCount; i++) {
                Object val = record.getValue(columns.get(i).getName());
                String str = val == null ? "NULL" : val.toString();
                sb.append(String.format("%-" + (colWidths[i] + 2) + "s", str));
            }
            sb.append(this.lineEndingText());
        }

        sb.append(records.size()).append(" row(s) in set");

        long ms = result.getUsedMs();
        if (ms > 0) {
            sb.append(" (").append(ms).append(" ms)");
        }
        return sb.toString();
    }

    @Override
    protected TerminalCommandHandler findHandler(String input) {
        TerminalCommandHandler<?, ?> handler = TerminalManager.findHandler(MysqlTerminalPane.TERMINAL_NAME, input);
        if (handler == null) {
            handler = new MysqlTerminalCommandHandler<>() {

                @Override
                public String commandName() {
                    return "";
                }

                @Override
                public TerminalExecuteResult execute(TerminalCommand command, MysqlTerminalPane terminal) {
                    String input = command.getContent();
                    return terminal.eval(input);
                }
            };
        }
        return handler;
    }

    public ShellConnect shellConnect() {
        return this.shellConnect;
    }

    @Override
    public void initNode() {
        this.keyHandler(MysqlTerminalKeyHandler.INSTANCE);
        this.helpHandler(MysqlTerminalHelpHandler.INSTANCE);
        this.mouseHandler(MysqlTerminalMouseHandler.INSTANCE);
        this.historyHandler(MysqlTerminalHistoryHandler.INSTANCE);
        this.completeHandler(MysqlTerminalCompleteHandler.INSTANCE);
        super.initNode();
    }
}
