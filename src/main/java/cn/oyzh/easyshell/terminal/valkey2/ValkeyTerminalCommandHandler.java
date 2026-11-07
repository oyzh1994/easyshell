package cn.oyzh.easyshell.terminal.valkey2;

import cn.oyzh.fx.terminal.command.BaseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;

import java.util.List;

/**
 * Valkey v2 终端命令基类。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public abstract class ValkeyTerminalCommandHandler<C extends TerminalCommand> extends BaseTerminalCommandHandler<C, ValkeyTerminalPane> {

    @Override
    public C parseCommand(String line) throws Exception {
        List<String> words = ValkeyCommandTokenizer.tokenize(line);
        return this.parseCommand(line, words.toArray(String[]::new));
    }
}
