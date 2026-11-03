package cn.oyzh.easyshell.terminal.sqlite.basic;

import cn.oyzh.easyshell.sqlite.database.SQLiteDatabase;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

/**
 * `.database` 命令。
 *
 * @since 2026-11-02
 */
public class SqliteDatabaseTerminalCommandHandler extends ShellSqliteTerminalCommandHandler<TerminalCommand> {

    @Override
    public String commandName() {
        return ".database";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ShellSqliteTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            SQLiteDatabase info = terminal.getClient().database();
            result.setResult("""
                    main: %s
                    version: %s
                    size: %d bytes
                    pages: %d x %d bytes
                    encoding: %s
                    journal_mode: %s
                    foreign_keys: %s
                    synchronous: %s
                    readonly: %s
                    """.formatted(info.getFilePath(), info.getVersion(), info.getFileSize(),
                    info.getPageCount(), info.getPageSize(), info.getEncoding(), info.getJournalMode(),
                    info.isForeignKeys(), info.getSynchronous(), info.isReadOnly()));
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
