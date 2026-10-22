package cn.oyzh.easyshell.terminal.redis2;

import cn.oyzh.easyshell.redis.ShellRedisCommandSession;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 执行任意 Redis 命令参数，不为每条 Redis 命令维护 Java 处理器。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class RedisCommandExecutor {

    /** 需要保持在同一事务会话中的 Redis 命令。 */
    private static final Set<String> TRANSACTION_COMMANDS = Set.of("multi", "exec", "discard", "watch", "unwatch");

    /**
     * 执行 Redis 命令。
     *
     * @param args       完整命令参数
     * @param descriptor 命令元数据，可为空
     * @param session    Redis 命令会话
     * @return Redis 原始返回值
     * @throws Exception 命令执行失败
     */
    public Object execute(List<String> args, RedisCommandDescriptor descriptor,
                          ShellRedisCommandSession session) throws Exception {
        List<String> keys = this.commandKeys(args, session);
        return session.execute(RedisCommandBuilder.build(args, descriptor, keys));
    }

    /**
     * 获取可用于 Jedis 路由的键参数。
     *
     * @param args    完整命令参数
     * @param session Redis 命令会话
     * @return 键参数列表
     */
    private List<String> commandKeys(List<String> args, ShellRedisCommandSession session) {
        String commandName = args.getFirst().toLowerCase(Locale.ROOT);
        if (session.isTransaction() || TRANSACTION_COMMANDS.contains(commandName)) {
            return List.of();
        }
        try {
            return session.commandGetKeys(args);
        } catch (Exception ignored) {
            return List.of();
        }
    }
}
