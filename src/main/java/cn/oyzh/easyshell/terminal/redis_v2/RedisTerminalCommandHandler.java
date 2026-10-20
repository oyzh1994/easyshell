package cn.oyzh.easyshell.terminal.redis_v2;

import cn.oyzh.fx.terminal.command.BaseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;

import java.util.List;

/**
 * Redis v2 终端命令基类。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public abstract class RedisTerminalCommandHandler<C extends TerminalCommand>
        extends BaseTerminalCommandHandler<C, RedisTerminalPane> {

    @Override
    public C parseCommand(String line) throws Exception {
        List<String> words = RedisCommandTokenizer.tokenize(line);
        return this.parseCommand(line, words.toArray(String[]::new));
    }
}
