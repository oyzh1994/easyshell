package cn.oyzh.easyshell.terminal.redis.acl;

import redis.clients.jedis.Protocol;

/**
 * Redis ACL SETUSER 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisAclSetuserTerminalCommandHandler extends RedisAclTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.SETUSER.name();
    }
}
