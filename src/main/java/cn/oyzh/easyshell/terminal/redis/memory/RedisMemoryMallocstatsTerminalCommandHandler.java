package cn.oyzh.easyshell.terminal.redis.memory;

/**
 * Redis MEMORY MALLOC-STATS 子命令处理器
 *
 * @author oyzh
 * @since 2025-09-01
 */

public class RedisMemoryMallocstatsTerminalCommandHandler extends RedisMemoryTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "MALLOC-STATS";
    }
}
