package cn.oyzh.easyshell.terminal.valkey2;

import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import io.valkey.resps.CommandDocument;
import io.valkey.resps.CommandInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * 从 Valkey COMMAND/INFO/DOCS 动态发现命令，避免维护命令类和参数硬编码。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public final class ValkeyCommandRegistry {

    /** 以规范化命令名称索引的命令元数据。 */
    private final Map<String, ValkeyCommandDescriptor> commands;

    private ValkeyCommandRegistry(Map<String, ValkeyCommandDescriptor> commands) {
        this.commands = Map.copyOf(commands);
    }

    /**
     * 从 Valkey 服务端加载命令元数据。
     *
     * @param client Valkey 客户端
     * @return 命令注册表
     */
    public static ValkeyCommandRegistry load(ShellValkeyClient client) {
        Map<String, CommandDocument> docs;
        try {
            docs = client.commandDocs();
        } catch (Exception ex) {
            docs = Map.of();
        }
        List<String> names;
        try {
            names = client.commandList();
        } catch (Exception ex) {
            names = List.of();
        }
        Map<String, CommandInfo> info = new LinkedHashMap<>();
        loadInfo(client, names, 0, names.size(), info);
        return from(names, info, docs);
    }

    /**
     * 分批加载命令信息。valkey-java 5.x 对部分命令的 COMMAND INFO 返回值解析存在差异，
     * 失败时拆分批次并逐条降级，保证终端命令补全和帮助始终可用。
     */
    private static void loadInfo(ShellValkeyClient client, List<String> names, int from, int to,
                                 Map<String, CommandInfo> info) {
        if (from >= to) {
            return;
        }
        if (to - from <= 50) {
            try {
                info.putAll(client.commandInfo(names.subList(from, to).toArray(new String[0])));
                return;
            } catch (Exception ignored) {
                if (to - from == 1) {
                    return;
                }
            }
        }
        int mid = (from + to) / 2;
        loadInfo(client, names, from, mid, info);
        loadInfo(client, names, mid, to, info);
    }

    /**
     * 使用命令信息创建注册表。
     *
     * @param info 命令信息
     * @return 命令注册表
     */
    public static ValkeyCommandRegistry from(Map<String, CommandInfo> info) {
        return from(info, Map.of());
    }

    /**
     * 使用命令信息和文档创建注册表。
     *
     * @param info 命令信息
     * @param docs 命令文档
     * @return 命令注册表
     */
    public static ValkeyCommandRegistry from(Map<String, CommandInfo> info, Map<String, CommandDocument> docs) {
        return from(new ArrayList<>(info.keySet()), info, docs);
    }

    /**
     * 使用命令名称和可用的命令信息、文档创建注册表。
     */
    private static ValkeyCommandRegistry from(List<String> names, Map<String, CommandInfo> info,
                                              Map<String, CommandDocument> docs) {
        Map<String, ValkeyCommandDescriptor> result = new TreeMap<>();
        Map<String, List<String>> subcommandNames = new LinkedHashMap<>();
        List<String> topNames = new ArrayList<>();
        for (String name : names) {
            int index = name.indexOf('|');
            if (index > 0) {
                subcommandNames.computeIfAbsent(name.substring(0, index), key -> new ArrayList<>()).add(name);
            } else {
                topNames.add(name);
            }
        }
        for (String name : topNames) {
            Map<String, ValkeyCommandDescriptor> subcommands = new LinkedHashMap<>();
            for (String subName : subcommandNames.getOrDefault(name, List.of())) {
                List<String> path = List.of(subName.split("\\|"));
                subcommands.put(normalize(path.getLast()),
                        descriptor(String.join(" ", path), path, info.get(normalize(subName)), docs));
            }
            result.put(normalize(name),
                    descriptor(name, List.of(name), info.get(normalize(name)), docs, subcommands));
        }
        return new ValkeyCommandRegistry(result);
    }

    /**
     * 获取全部命令元数据。
     *
     * @return 命令元数据集合
     */
    public Collection<ValkeyCommandDescriptor> commands() {
        return this.commands.values();
    }

    /**
     * 获取排序后的命令名称。
     *
     * @return 命令名称列表
     */
    public List<String> commandNames() {
        return this.commands.values().stream().map(ValkeyCommandDescriptor::displayName).sorted().toList();
    }

    /**
     * 按命令及子命令路径查找元数据。
     *
     * @param tokens 命令词组
     * @return 最接近输入的命令元数据，未找到时返回 {@code null}
     */
    public ValkeyCommandDescriptor find(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        ValkeyCommandDescriptor current = this.commands.get(normalize(tokens.getFirst()));
        for (int index = 1; current != null && index < tokens.size(); index++) {
            ValkeyCommandDescriptor next = current.subcommands().get(normalize(tokens.get(index)));
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
    public List<ValkeyCommandDescriptor> complete(List<String> tokens) {
        List<String> parts = tokens == null ? List.of() : tokens;
        if (parts.isEmpty()) {
            return new ArrayList<>(this.commands.values());
        }
        if (parts.size() == 1) {
            String prefix = normalize(parts.getFirst());
            return this.commands.values().stream()
                    .filter(command -> normalize(matchName(command)).startsWith(prefix))
                    .toList();
        }
        ValkeyCommandDescriptor parent = this.find(parts.subList(0, parts.size() - 1));
        if (parent == null) {
            return List.of();
        }
        String prefix = normalize(parts.getLast());
        return parent.subcommands().values().stream()
                .filter(command -> normalize(matchName(command)).startsWith(prefix))
                .toList();
    }

    /**
     * 获取用于补全匹配的命令名称。
     */
    private static String matchName(ValkeyCommandDescriptor command) {
        return command.path().isEmpty() ? command.name() : command.path().getLast();
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
    private static ValkeyCommandDescriptor descriptor(String name, List<String> path, CommandInfo info,
                                                      Map<String, CommandDocument> docs) {
        return descriptor(name, path, info, docs, null);
    }

    private static ValkeyCommandDescriptor descriptor(String name, List<String> path, CommandInfo info,
                                                      Map<String, CommandDocument> docs,
                                                      Map<String, ValkeyCommandDescriptor> knownSubcommands) {
        String displayName = String.join(" ", path);
        CommandDocument document = docs.get(normalize(name));
        if (document == null) {
            document = docs.get(normalize(displayName));
        }
        Map<String, ValkeyCommandDescriptor> subcommands = new LinkedHashMap<>();
        if (knownSubcommands != null) {
            subcommands.putAll(knownSubcommands);
        }
        // valkey-java 5.x 的 COMMAND 返回值只包含子命令名称，不含子命令详情
        if (info != null && info.getSubcommands() != null) {
            for (String subName : info.getSubcommands()) {
                if (subcommands.containsKey(normalize(subName))) {
                    continue;
                }
                List<String> subPath = new ArrayList<>(path);
                subPath.add(subName);
                subcommands.put(normalize(subName), new ValkeyCommandDescriptor(
                        String.join(" ", subPath),
                        subPath,
                        -1,
                        List.of(),
                        0,
                        0,
                        0,
                        null,
                        Map.of()
                ));
            }
        }
        return new ValkeyCommandDescriptor(
                displayName,
                path,
                info == null ? -1 : info.getArity(),
                info == null ? List.of() : info.getFlags(),
                info == null ? 0 : info.getFirstKey(),
                info == null ? 0 : info.getLastKey(),
                info == null ? 0 : info.getStep(),
                document == null ? null : document.getSummary(),
                subcommands
        );
    }

    /**
     * 优先使用 Valkey 返回的正式命令名称。
     *
     * @param key  注册表键
     * @param info 命令信息
     * @return 命令名称
     */
    private static String name(String key, CommandInfo info) {
        return key;
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
