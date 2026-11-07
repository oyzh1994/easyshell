package cn.oyzh.easyshell.terminal.valkey2;

import cn.oyzh.easyshell.valkey.ShellValkeyCommandSession;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 执行任意 Valkey 命令参数，不为每条 Valkey 命令维护 Java 处理器。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class ValkeyCommandExecutor {

    /** 需要保持在同一事务会话中的 Valkey 命令。 */
    private static final Set<String> TRANSACTION_COMMANDS = Set.of("multi", "exec", "discard", "watch", "unwatch");

    /**
     * 执行 Valkey 命令。
     *
     * @param args       完整命令参数
     * @param descriptor 命令元数据，可为空
     * @param session    Valkey 命令会话
     * @return Valkey 原始返回值
     * @throws Exception 命令执行失败
     */
    public Object execute(List<String> args, ValkeyCommandDescriptor descriptor,
                          ShellValkeyCommandSession session) throws Exception {
        List<String> keys = this.commandKeys(args, session);
        return session.execute(ValkeyCommandBuilder.build(args, descriptor, keys));
    }

    /**
     * 获取可用于 Jedis 路由的键参数。
     *
     * @param args    完整命令参数
     * @param session Valkey 命令会话
     * @return 键参数列表
     */
    private List<String> commandKeys(List<String> args, ShellValkeyCommandSession session) {
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
