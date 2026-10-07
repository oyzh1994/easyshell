package cn.oyzh.easyshell.terminal.redis.acl;

import redis.clients.jedis.Protocol;

/**
 * Redis ACL DRYRUN 子命令处理器
 *
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisAclDryrunTerminalCommandHandler extends RedisAclTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.DRYRUN.name();
    }
}
