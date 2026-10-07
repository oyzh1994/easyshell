package cn.oyzh.easyshell.terminal.redis.acl;

import redis.clients.jedis.Protocol;

/**
 * Redis ACL WHOAMI 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisAclWhoamiTerminalCommandHandler extends RedisAclTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.WHOAMI.name();
    }
}
