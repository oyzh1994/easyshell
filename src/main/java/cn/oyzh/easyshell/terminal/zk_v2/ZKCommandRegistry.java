package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ClassUtil;
import org.apache.zookeeper.ZooKeeperMain;
import org.apache.zookeeper.cli.CliCommand;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 动态发现 ZooKeeper CLI 命令，避免逐命令硬编码。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public final class ZKCommandRegistry {

    /** ZooKeeper 服务端支持的四字命令。 */
    private static final Set<String> FOUR_LETTER_WORD_COMMANDS = Set.of(
            "conf", "cons", "crst", "dirs", "dump", "envi", "kill", "mntr",
            "reqs", "ruok", "srst", "srvr", "stat4", "wchc", "wchp", "wchs"
    );
    /** 为兼容旧版终端保留的 CLI 命令别名。 */
    private static final Map<String, String> LEGACY_CLI_ALIASES = Map.of(
            "ls2", "ls",
            "rmr", "deleteall"
    );

    /** 全部动态命令元数据。 */
    private static final Map<String, ZKCommandDescriptor> COMMANDS = loadCommands();
    /** 可实例化的 ZooKeeper CLI 命令类型。 */
    private static final Map<String, CliCommandType> CLI_COMMANDS = loadCliCommands();

    private ZKCommandRegistry() {
    }

    /**
     * 加载 ZooKeeper CLI 命令类型并补充兼容别名。
     *
     * @return CLI 命令类型映射
     */
    private static Map<String, CliCommandType> loadCliCommands() {
        Map<String, CliCommandType> commands = new LinkedHashMap<>();
        for (Map.Entry<String, CliCommand> entry : cliCommandMap().entrySet()) {
            CliCommand command = entry.getValue();
            commands.put(normalize(entry.getKey()), new CliCommandType(command.getCmdStr(), command.getClass()));
        }
        LEGACY_CLI_ALIASES.forEach((alias, commandName) -> {
            CliCommandType commandType = commands.get(normalize(commandName));
            if (commandType != null) {
                commands.putIfAbsent(normalize(alias), commandType);
            }
        });
        return Map.copyOf(commands);
    }

    /**
     * 加载 CLI、四字命令和终端辅助命令元数据。
     *
     * @return 命令元数据映射
     */
    private static Map<String, ZKCommandDescriptor> loadCommands() {
        Map<String, ZKCommandDescriptor> commands = new TreeMap<>();
        for (Map.Entry<String, CliCommand> entry : cliCommandMap().entrySet()) {
            CliCommand command = entry.getValue();
            String name = normalize(entry.getKey());
            commands.put(name, new ZKCommandDescriptor(entry.getKey(), command.getOptionStr(), ZKCommandDescriptor.Type.CLI));
        }
        LEGACY_CLI_ALIASES.forEach((alias, commandName) -> {
            ZKCommandDescriptor command = commands.get(normalize(commandName));
            if (command != null) {
                commands.putIfAbsent(normalize(alias),
                        new ZKCommandDescriptor(alias, command.usage(), ZKCommandDescriptor.Type.CLI));
            }
        });
        for (String command : FOUR_LETTER_WORD_COMMANDS) {
            commands.put(command, new ZKCommandDescriptor(command, "", ZKCommandDescriptor.Type.FOUR_LETTER_WORD));
        }
        commands.put("connect", new ZKCommandDescriptor("connect", "[-timeout timeout] [-server server] [-r]", ZKCommandDescriptor.Type.TERMINAL));
        commands.put("version", new ZKCommandDescriptor("version", "", ZKCommandDescriptor.Type.TERMINAL));
        return Map.copyOf(commands);
    }

    /**
     * 从 ZooKeeper 内置注册表和类路径中收集 CLI 命令。
     *
     * @return CLI 命令实例映射
     */
    private static Map<String, CliCommand> cliCommandMap() {
        Map<String, CliCommand> commands = new LinkedHashMap<>();
        try {
            commands.putAll(ZooKeeperMain.commandMapCli);
            ClassUtil.scanClasses(CliCommand.class.getPackageName(), (Predicate<Class<?>>) aClass -> {
                if (!Modifier.isAbstract(aClass.getModifiers()) && CliCommand.class.isAssignableFrom(aClass)) {
                    CliCommand command = (CliCommand) ClassUtil.newInstance(aClass);
                    if (command != null) {
                        commands.putIfAbsent(command.getCmdStr(), command);
                    } else {
                        JulLog.warn("init CliCommand:{} fail", aClass);
                    }
                }
                return false;
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commands;
    }

    /**
     * 查找命令元数据。
     *
     * @param command 命令名称
     * @return 命令元数据
     */
    public static Optional<ZKCommandDescriptor> find(String command) {
        return Optional.ofNullable(COMMANDS.get(normalize(command)));
    }

    /**
     * 判断命令是否存在。
     *
     * @param command 命令名称
     * @return 是否存在
     */
    public static boolean contains(String command) {
        return COMMANDS.containsKey(normalize(command));
    }

    /**
     * 获取全部命令元数据。
     *
     * @return 命令元数据集合
     */
    public static Collection<ZKCommandDescriptor> commands() {
        return COMMANDS.values();
    }

    /**
     * 获取全部命令名称。
     *
     * @return 命令名称集合
     */
    public static Set<String> commandNames() {
        return COMMANDS.keySet();
    }

    /**
     * 查找指定名称前缀的命令。
     *
     * @param prefix 命令名称前缀
     * @return 命令元数据列表
     */
    public static List<ZKCommandDescriptor> findCommands(String prefix) {
        String normalizedPrefix = normalize(prefix);
        return COMMANDS.values().stream()
                .filter(command -> normalize(command.name()).startsWith(normalizedPrefix))
                .toList();
    }

    /**
     * 获取可实例化的 ZooKeeper CLI 命令类型。
     *
     * @param command 命令名称
     * @return CLI 命令类型
     */
    public static CliCommandType cliCommandType(String command) {
        return CLI_COMMANDS.get(normalize(command));
    }

    /**
     * 规范化命令名称。
     *
     * @param command 原始命令名称
     * @return 小写命令名称
     */
    private static String normalize(String command) {
        return command == null ? "" : command.toLowerCase(Locale.ROOT);
    }

    /**
     * 保存 ZooKeeper 命令类型并按需创建新命令实例。
     *
     * @param commandName  命令名称
     * @param commandClass 命令实现类
     */
    public record CliCommandType(String commandName, Class<? extends CliCommand> commandClass) {

        /**
         * 创建新的命令实例，避免多个终端共享可变状态。
         *
         * @return ZooKeeper CLI 命令实例
         */
        public CliCommand newCommand() {
            try {
                Constructor<? extends CliCommand> constructor = commandClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Unable to create ZooKeeper command: " + commandName, ex);
            }
        }
    }
}
