package cn.oyzh.easyshell.terminal.redis.acl;

import cn.oyzh.easyshell.terminal.redis.acl.RedisAclTerminalCommandHandler;
import redis.clients.jedis.Protocol;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisAclListTerminalCommandHandler extends RedisAclTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.LIST.name();
    }
}
