package cn.oyzh.easyshell.terminal.h2.basic;

import cn.oyzh.easyshell.h2.schema.H2Schema;
import cn.oyzh.easyshell.terminal.h2.ShellH2TerminalCommandHandler;
import cn.oyzh.easyshell.terminal.h2.ShellH2TerminalPane;
import cn.oyzh.fx.db.util.DBTerminalUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;

/**
 * H2 切换 schema 命令处理器。
 *
 * <p>H2 单库多 schema，`use &lt;schema&gt;` 切换终端当前 schema。</p>
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class H2UseTerminalCommandHandler extends ShellH2TerminalCommandHandler<TerminalCommand> {

    @Override
    protected TerminalCommand parseCommand(String line, String[] args) {
        TerminalCommand terminalCommand = new TerminalCommand();
        terminalCommand.setArgs(args);
        terminalCommand.setCommand(line);
        return terminalCommand;
    }

    @Override
    protected boolean checkArgs(String[] args) throws RuntimeException {
        return args != null && args.length == 2;
    }

    @Override
    public String commandName() {
        return "use";
    }

    @Override
    public String commandSubName() {
        return "<schema>;";
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ShellH2TerminalPane terminal) {
        String schemaName = DBTerminalUtil.fixArg(command.getArgs()[1]);
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        try {
            List<String> schemas = terminal.getClient().selectSchemas().stream()
                    .map(H2Schema::getName)
                    .toList();
            String schema = schemas.stream()
                    .filter(item -> item.equalsIgnoreCase(schemaName))
                    .findFirst()
                    .orElse(null);
            if (schema != null) {
                terminal.setDbName(schema);
                result.setResult("Schema changed");
            } else {
                result.setResult("Schema invalid");
            }
        } catch (Exception ex) {
            result.setException(ex);
        }
        return result;
    }
}
