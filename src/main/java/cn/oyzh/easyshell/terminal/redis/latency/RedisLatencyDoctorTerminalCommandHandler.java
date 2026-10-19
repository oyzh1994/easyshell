package cn.oyzh.easyshell.terminal.redis.latency;

import redis.clients.jedis.Protocol;

/**
 * Redis LATENCY DOCTOR 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisLatencyDoctorTerminalCommandHandler extends RedisLatencyTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return Protocol.Keyword.DOCTOR.name();
    }
}
