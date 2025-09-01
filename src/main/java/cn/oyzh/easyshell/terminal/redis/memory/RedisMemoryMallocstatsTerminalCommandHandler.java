package cn.oyzh.easyshell.terminal.redis.memory;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisMemoryMallocstatsTerminalCommandHandler extends RedisMemoryTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "MALLOC-STATS";
    }
}
