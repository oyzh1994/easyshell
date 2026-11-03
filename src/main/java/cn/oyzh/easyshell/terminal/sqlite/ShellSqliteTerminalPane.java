package cn.oyzh.easyshell.terminal.sqlite;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.sqlite.ShellSqliteHelper;
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

/**
 * SQLite SQL 终端。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTerminalPane extends TerminalPane {

    public static final String TERMINAL_NAME = "sqlite";

    private ShellSqliteClient client;

    public void init(ShellSqliteClient client) {
        this.client = client;
        FXUtil.runPulse(() -> {
            this.disableInput();
            this.outputLine(ShellI18nHelper.welcome());
            this.outputLine("SQLite " + client.selectVersion() + " - " + client.getShellConnect().getHost());
            this.flushPrompt();
            this.appendByPrompt("");
            this.enableInput();
            this.flushAndMoveCaretEnd();
        });
    }

    public ShellSqliteClient getClient() {
        return this.client;
    }

    public ShellConnect shellConnect() {
        return this.client.getShellConnect();
    }

    @Override
    public void flushPrompt() {
        String name = this.shellConnect().getName();
        if (this.isConnected()) {
            this.prompt(name + "(" + I18nHelper.connected() + ")> ");
        } else {
            this.prompt(name + "> ");
        }
    }

    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
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
            DBQueryResults<SQLiteExecuteResult> results = this.client.executeSql(input);
            if (!results.isSuccess()) {
                terminalResult.setException(new RuntimeException(results.getErrMsg()));
            } else {
                terminalResult.setResult(ShellSqliteHelper.formatResults(results));
            }
        } catch (Exception ex) {
            terminalResult.setException(ex);
        }
        return terminalResult;
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
