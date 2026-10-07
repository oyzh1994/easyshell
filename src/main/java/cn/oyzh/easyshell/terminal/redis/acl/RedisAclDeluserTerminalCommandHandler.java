package cn.oyzh.easyshell.terminal.redis.acl;

import redis.clients.jedis.Protocol;

/**
 * Redis ACL DELUSER 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisAclDeluserTerminalCommandHandler extends RedisAclTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.DELUSER.name();
    }
}
