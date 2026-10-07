package cn.oyzh.easyshell.terminal.redis.base;

import cn.oyzh.easyshell.terminal.redis.RedisNKeysTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis RESTORE 命令处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */

public class RedisRestoreTerminalCommandHandler extends RedisNKeysTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.RESTORE;
    }


}
