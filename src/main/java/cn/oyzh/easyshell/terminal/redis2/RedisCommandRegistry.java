package cn.oyzh.easyshell.terminal.redis2;

import cn.oyzh.easyshell.redis.ShellRedisClient;
import redis.clients.jedis.resps.CommandDocument;
import redis.clients.jedis.resps.CommandInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * 从 Redis COMMAND/INFO/DOCS 动态发现命令，避免维护命令类和参数硬编码。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public final class RedisCommandRegistry {

    /** 以规范化命令名称索引的命令元数据。 */
    private final Map<String, RedisCommandDescriptor> commands;

    private RedisCommandRegistry(Map<String, RedisCommandDescriptor> commands) {
        this.commands = Map.copyOf(commands);
    }

    /**
     * 从 Redis 服务端加载命令元数据。
     *
     * @param client Redis 客户端
     * @return 命令注册表
     */
    public static RedisCommandRegistry load(ShellRedisClient client) {
        Map<String, CommandInfo> info = client.commandInfo();
        Map<String, CommandDocument> docs;
        try {
            docs = client.commandDocs();
        } catch (Exception ex) {
            docs = Map.of();
        }
        return from(info, docs);
    }

    /**
     * 使用 {@code COMMAND} 返回值创建命令注册表。
     *
     * @param info Redis 命令信息
     * @return 命令注册表
     */
    public static RedisCommandRegistry from(Map<String, CommandInfo> info) {
        return from(info, Map.of());
    }

    /**
     * 使用命令信息和文档创建命令注册表。
     *
     * @param info Redis 命令信息
     * @param docs Redis 命令文档
     * @return 命令注册表
     */
    public static RedisCommandRegistry from(Map<String, CommandInfo> info, Map<String, CommandDocument> docs) {
        Map<String, RedisCommandDescriptor> result = new TreeMap<>();
        for (Map.Entry<String, CommandInfo> entry : info.entrySet()) {
            String name = name(entry.getKey(), entry.getValue());
            result.put(normalize(name), descriptor(name, List.of(name), entry.getValue(), docs));
        }
        return new RedisCommandRegistry(result);
    }

    /**
     * 获取全部命令元数据。
     *
     * @return 命令元数据集合
     */
    public Collection<RedisCommandDescriptor> commands() {
        return this.commands.values();
    }

    /**
     * 获取排序后的命令名称。
     *
     * @return 命令名称列表
     */
    public List<String> commandNames() {
        return this.commands.values().stream().map(RedisCommandDescriptor::displayName).sorted().toList();
    }

    /**
     * 按命令及子命令路径查找元数据。
     *
     * @param tokens 命令词组
     * @return 最接近输入的命令元数据，未找到时返回 {@code null}
     */
    public RedisCommandDescriptor find(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        RedisCommandDescriptor current = this.commands.get(normalize(tokens.getFirst()));
        for (int index = 1; current != null && index < tokens.size(); index++) {
            RedisCommandDescriptor next = current.subcommands().get(normalize(tokens.get(index)));
            if (next == null) {
                break;
            }
            current = next;
        }
        return current;
    }

    /**
     * 查找指定前缀的命令或子命令。
     *
     * @param tokens 已输入的命令词组
     * @return 可补全的命令元数据
     */
    public List<RedisCommandDescriptor> complete(List<String> tokens) {
        List<String> parts = tokens == null ? List.of() : tokens;
        if (parts.isEmpty()) {
            return new ArrayList<>(this.commands.values());
        }
        if (parts.size() == 1) {
            String prefix = normalize(parts.getFirst());
            return this.commands.values().stream()
                    .filter(command -> normalize(command.name()).startsWith(prefix))
                    .toList();
        }
        RedisCommandDescriptor parent = this.find(parts.subList(0, parts.size() - 1));
        if (parent == null) {
            return List.of();
        }
        String prefix = normalize(parts.getLast());
        return parent.subcommands().values().stream()
                .filter(command -> normalize(command.name()).startsWith(prefix))
                .toList();
    }

    /**
     * 递归构建命令及子命令元数据。
     *
     * @param name  当前命令名称
     * @param path  当前命令路径
     * @param info  当前命令信息
     * @param docs  命令文档
     * @return 命令元数据
     */
    private static RedisCommandDescriptor descriptor(String name, List<String> path, CommandInfo info,
                                                      Map<String, CommandDocument> docs) {
        String displayName = String.join(" ", path);
        CommandDocument document = docs.get(normalize(name));
        if (document == null) {
            document = docs.get(normalize(displayName));
        }
        Map<String, RedisCommandDescriptor> subcommands = new LinkedHashMap<>();
        if (info.getSubcommands() != null) {
            for (Map.Entry<String, CommandInfo> entry : info.getSubcommands().entrySet()) {
                String subName = name(entry.getKey(), entry.getValue());
                List<String> subPath = new ArrayList<>(path);
                subPath.add(subName);
                subcommands.put(normalize(subName), descriptor(subName, subPath, entry.getValue(), docs));
            }
        }
        return new RedisCommandDescriptor(
                displayName,
                path,
                info.getArity(),
                info.getFlags(),
                info.getFirstKey(),
                info.getLastKey(),
                info.getStep(),
                document == null ? null : document.getSummary(),
                subcommands
        );
    }

    /**
     * 优先使用 Redis 返回的正式命令名称。
     *
     * @param key  注册表键
     * @param info 命令信息
     * @return 命令名称
     */
    private static String name(String key, CommandInfo info) {
        return info.getName() == null || info.getName().isBlank() ? key : info.getName();
    }

    /**
     * 规范化命令名称。
     *
     * @param value 原始名称
     * @return 小写名称
     */
    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
