package cn.oyzh.easyshell.terminal.redis2.basic;

import cn.oyzh.easyshell.terminal.redis2.RedisTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.redis2.RedisTerminalPane;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.i18n.I18nHelper;

/**
 * 临时 Redis 终端连接命令。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class RedisConnectCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    /**
     * 校验 connect 命令至少包含连接参数。
     */
    @Override
    protected boolean checkArgs(String[] words) {
        return words != null && words.length >= 2;
    }

    /**
     * 为临时终端建立 Redis 连接。
     */
    @Override
    public TerminalExecuteResult execute(TerminalCommand command, RedisTerminalPane terminal) {
        if (!terminal.isTemporary()) {
            terminal.outputByPrompt(I18nHelper.operationNotSupport());
        } else {
            if (terminal.isConnected()) {
                terminal.getClient().close();
            }
            terminal.connect(command.getContent());
        }
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        result.setIgnoreOutput(true);
        return result;
    }

    @Override
    public String commandName() {
        return "connect";
    }

    @Override
    public String commandArg() {
        return "[-timeout timeout] [-h host] [-p port] [-u user] [-a password] [-n db] [-r]";
    }
}
