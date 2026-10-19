package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.easyshell.terminal.zk.ZKTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.util.TerminalUtil;
import org.apache.zookeeper.cli.CliCommand;

/**
 * zk cli命令处理器
 *
 * @author oyzh
 * @since 2025-09-04
 */
public abstract class ZKCliTerminalCommandHandler<C extends TerminalCommand> extends ZKTerminalCommandHandler<C> {

    /**
     * 获取cli命令
     *
     * @return cli命令
     */
    protected abstract CliCommand cliCommand();

    @Override
    public C parseCommand(String line) {
        String[] args = TerminalUtil.split(line);
        TerminalCommand command = new TerminalCommand();
        command.setArgs(args);
        return (C) command;
    }

    @Override
    public TerminalExecuteResult execute(C command, ZKTerminalPane terminal) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        try {
            terminal.disable();
            ZKCliCommandWrapper wrapper = new ZKCliCommandWrapper(this.cliCommand(), terminal.zooKeeper(), terminal.lineEndingText());
            wrapper.parse(command.getArgs());
            wrapper.setOnResponse(result::appendResult);
            wrapper.exec();
        } catch (Exception ex) {
            result.setException(ex);
        } finally {
            terminal.enable();
        }
        return result;
    }
}
