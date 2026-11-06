package cn.oyzh.easyshell.terminal.postgresql;

import cn.oyzh.fx.terminal.command.BaseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * PostgreSQL终端命令处理器基类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public abstract class PostgresqlTerminalCommandHandler<C extends TerminalCommand> extends BaseTerminalCommandHandler<C, PostgresqlTerminalPane> {

}
