package cn.oyzh.easyshell.terminal.redis.cluster;

/**
 * @author oyzh
 * @since 2023/7/31
 */

public class RedisClusterAddSlotsRangeTerminalCommandHandler extends RedisClusterTerminalCommandHandler {

    @Override
    public String commandSubName() {
        return "ADDSLOTSRANGE";
    }
}
