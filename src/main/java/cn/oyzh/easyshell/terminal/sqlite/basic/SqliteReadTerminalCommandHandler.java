package cn.oyzh.easyshell.terminal.sqlite.basic;

import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.sqlite.ShellSqliteTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * `.read` 命令。
 *
 * @since 2026-11-02
 */
public class SqliteReadTerminalCommandHandler extends ShellSqliteTerminalCommandHandler<TerminalCommand> {

    @Override
    public String commandName() {
        return ".read";
    }

    @Override
    protected boolean checkArgs(String[] args) {
        return args != null && args.length == 2;
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ShellSqliteTerminalPane terminal) {
        try {
            Path file = Path.of(command.getArgs()[0]).toAbsolutePath().normalize();
            if (!Files.isRegularFile(file)) {
                throw new IllegalArgumentException("SQL file does not exist: " + file);
            }
            return terminal.eval(Files.readString(file));
        } catch (Exception ex) {
            TerminalExecuteResult result = new TerminalExecuteResult();
            result.setException(ex);
            return result;
        }
    }
}
