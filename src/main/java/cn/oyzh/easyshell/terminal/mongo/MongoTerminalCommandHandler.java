package cn.oyzh.easyshell.terminal.mongo;

import cn.oyzh.fx.terminal.command.BaseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * mongo终端命令处理器基类
 *
 * @author oyzh
 * @since 2026-06-29
 */
public abstract class MongoTerminalCommandHandler<C extends TerminalCommand> extends BaseTerminalCommandHandler<C, MongoTerminalPane> {

}
