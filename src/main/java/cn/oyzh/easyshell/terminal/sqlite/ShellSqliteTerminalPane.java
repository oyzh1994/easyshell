package cn.oyzh.easyshell.terminal.sqlite;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.query.sqlite.ShellSqliteQueryUtil;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
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
 * SQLite SQL 终端。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTerminalPane extends TerminalPane {

    public static final String TERMINAL_NAME = "sqlite";

    static {
        TerminalManager.setLoadHandler(TERMINAL_NAME, ShellSqliteTerminalManager::registerHandlers);
    }

    private ShellSqliteClient client;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String getDbName() {
        return this.dbName;
    }

    /**
     * 设置数据库名称并刷新提示符
     *
     * @param dbName 数据库名称
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
        this.flushPrompt();
    }

    /**
     * 初始化
     *
     * @param client 客户端
     * @param dbName 数据库名称
     */
    public void init(ShellSqliteClient client, String dbName) {
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

    public ShellSqliteClient getClient() {
        return this.client;
    }

    /**
     * 是否临时连接
     *
     * @return 结果
     */
    public boolean isTemporary() {
        return this.client == null || this.client.iid() == null;
    }

    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    @Override
    public void flushPrompt() {
        String str = this.dbName;
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

    @Override
    public void outputPrompt() {
        if (!this.isConnecting()) {
            super.outputPrompt();
        }
    }

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
    public String terminalName() {
        return TERMINAL_NAME;
    }

    @Override
    public Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.terminalFontConfig());
    }

    /**
     * 执行SQL。
     *
     * @param input 输入
     * @return 执行结果
     */
    public TerminalExecuteResult eval(String input) {
        TerminalExecuteResult terminalResult = new TerminalExecuteResult();
        try {
            if (this.dbName == null) {
                terminalResult.setResult("No database selected.");
                return terminalResult;
            }
            DBQueryResults<SQLiteExecuteResult> results = this.client.executeSql(input);
            if (!results.isSuccess()) {
                terminalResult.setException(new RuntimeException(results.getErrMsg()));
            } else if (results.isEmpty()) {
                terminalResult.setResult("OK");
            } else {
                StringBuilder sb = new StringBuilder();
                for (SQLiteExecuteResult result : results.getResults()) {
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
    private String formatResultSet(SQLiteExecuteResult result) {
        StringBuilder sb = new StringBuilder();
        SQLiteColumns columns = result.getColumns();
        List<SQLiteRecord> records = result.getRecords();
        if (columns == null || records == null) {
            return "";
        }

        int colCount = columns.size();
        int[] colWidths = new int[colCount];
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = Math.max(colWidths[i], columns.get(i).getName().length());
        }
        for (SQLiteRecord record : records) {
            for (int i = 0; i < colCount; i++) {
                Object val = record.getValue(columns.get(i));
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
        for (SQLiteRecord record : records) {
            for (int i = 0; i < colCount; i++) {
                Object val = record.getValue(columns.get(i));
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
    protected TerminalCommandHandler<?, ?> findHandler(String input) {
        TerminalCommandHandler<?, ?> handler = TerminalManager.findHandler(TERMINAL_NAME, input);
        if (handler != null) {
            return handler;
        }
        return new ShellSqliteTerminalCommandHandler<TerminalCommand>() {
            @Override
            public String commandName() {
                return "";
            }

            @Override
            public TerminalExecuteResult execute(TerminalCommand command, ShellSqliteTerminalPane terminal) {
                return terminal.eval(command.getContent());
            }
        };
    }

    @Override
    public void initNode() {
        this.keyHandler(ShellSqliteTerminalKeyHandler.INSTANCE);
        this.helpHandler(ShellSqliteTerminalHelpHandler.INSTANCE);
        this.mouseHandler(ShellSqliteTerminalMouseHandler.INSTANCE);
        this.historyHandler(ShellTerminalHistoryHandler.INSTANCE);
        this.completeHandler(ShellSqliteTerminalCompleteHandler.INSTANCE);
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
            prompts.addAll(ShellSqliteQueryUtil.getKeywords());
            this.setPrompts(prompts);
        }
        return super.getPrompts();
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
}
