package cn.oyzh.easyshell.terminal.redis2;

import redis.clients.jedis.BuilderFactory;
import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.CommandObject;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 根据 Redis 原生命令参数和动态元数据构建 Jedis 命令。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public final class RedisCommandBuilder {

    private RedisCommandBuilder() {
    }

    /**
     * 构建可由 Jedis 执行的原生 Redis 命令。
     *
     * @param args       完整命令参数
     * @param descriptor 命令元数据
     * @param keys       服务端返回的键参数
     * @return Jedis 命令对象
     */
    public static CommandObject<Object> build(List<String> args, RedisCommandDescriptor descriptor, List<String> keys) {
        if (args == null || args.isEmpty() || args.getFirst().isBlank()) {
            throw new IllegalArgumentException("No command entered");
        }
        CommandArguments arguments = new CommandArguments(command(args.getFirst()));
        Set<Integer> keyPositions = keyPositions(args, keys, descriptor);
        for (int index = 1; index < args.size(); index++) {
            if (keyPositions.contains(index)) {
                arguments.key(args.get(index));
            } else {
                arguments.add(args.get(index));
            }
        }
        if (isBlocking(descriptor)) {
            arguments.blocking();
        }
        return new CommandObject<>(arguments, BuilderFactory.RAW_OBJECT);
    }

    /**
     * 创建任意名称的 Redis 协议命令。
     *
     * @param name 命令名称
     * @return 协议命令
     */
    private static ProtocolCommand command(String name) {
        byte[] raw = SafeEncoder.encode(name);
        return () -> raw;
    }

    /**
     * 计算命令参数中需要标记为键的位置。
     *
     * @param args       完整命令参数
     * @param keys       服务端返回的键参数
     * @param descriptor 命令元数据
     * @return 键参数下标集合
     */
    private static Set<Integer> keyPositions(List<String> args, List<String> keys, RedisCommandDescriptor descriptor) {
        Set<Integer> positions = new HashSet<>();
        if (keys != null && !keys.isEmpty()) {
            int searchFrom = 1;
            for (String key : keys) {
                int found = -1;
                for (int index = searchFrom; index < args.size(); index++) {
                    if (args.get(index).equals(key)) {
                        found = index;
                        break;
                    }
                }
                if (found >= 0) {
                    positions.add(found);
                    searchFrom = found + 1;
                }
            }
        }
        if (!positions.isEmpty() || descriptor == null) {
            return positions;
        }
        long first = descriptor.firstKey();
        long last = descriptor.lastKey();
        long step = descriptor.step() <= 0 ? 1 : descriptor.step();
        if (first <= 0) {
            return positions;
        }
        if (last < 0) {
            last = args.size() - 1;
        }
        for (long index = first; index <= last && index < args.size(); index += step) {
            positions.add((int) index);
        }
        return positions;
    }

    /**
     * 判断命令是否需要使用阻塞模式执行。
     *
     * @param descriptor 命令元数据
     * @return 是否阻塞
     */
    private static boolean isBlocking(RedisCommandDescriptor descriptor) {
        return descriptor != null && descriptor.blocking();
    }
}
