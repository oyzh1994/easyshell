package cn.oyzh.easyshell.terminal.redis2;

import java.util.List;
import java.util.Map;

/**
 * Redis 命令元数据。
 *
 * @param name        命令完整名称
 * @param path        命令及子命令路径
 * @param arity       命令参数数量约束
 * @param flags       Redis 返回的命令标记
 * @param firstKey    第一个键参数位置
 * @param lastKey     最后一个键参数位置
 * @param step        键参数位置步长
 * @param summary     命令摘要
 * @param subcommands 子命令元数据
 * @author oyzh
 * @since 2026-10-20
 */
public record RedisCommandDescriptor(
        String name,
        List<String> path,
        long arity,
        List<String> flags,
        long firstKey,
        long lastKey,
        long step,
        String summary,
        Map<String, RedisCommandDescriptor> subcommands
) {

    /** 规范化空集合，确保命令元数据不可变。 */
    public RedisCommandDescriptor {
        path = path == null ? List.of(name) : List.copyOf(path);
        flags = flags == null ? List.of() : List.copyOf(flags);
        subcommands = subcommands == null ? Map.of() : Map.copyOf(subcommands);
    }

    /**
     * 判断命令是否为阻塞命令。
     *
     * @return 是否阻塞
     */
    public boolean blocking() {
        return flags.stream().anyMatch(flag -> flag.equalsIgnoreCase("blocking"));
    }

    /**
     * 获取包含子命令路径的显示名称。
     *
     * @return 命令显示名称
     */
    public String displayName() {
        return String.join(" ", path);
    }

    /**
     * 获取终端帮助中的命令用法。
     *
     * @return 命令用法
     */
    public String usage() {
        String displayName = this.displayName();
        if (arity == 1) {
            return displayName;
        }
        return displayName + " [arguments]";
    }
}
