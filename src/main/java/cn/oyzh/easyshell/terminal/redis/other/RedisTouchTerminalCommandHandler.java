package cn.oyzh.easyshell.terminal.redis.other;

import cn.oyzh.easyshell.terminal.redis.RedisNKeysTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import redis.clients.jedis.Protocol;

/**
 * Redis TOUCH 命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisTouchTerminalCommandHandler extends RedisNKeysTerminalCommandHandler<TerminalCommand> {

    @Override
    public Protocol.Command getCommandType() {
        return Protocol.Command.TOUCH;
    }
}
