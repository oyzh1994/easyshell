package cn.oyzh.easyshell.terminal.zk2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ClassUtil;
import org.apache.zookeeper.ZooKeeperMain;
import org.apache.zookeeper.cli.CliCommand;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 动态发现 ZooKeeper CLI 命令，避免逐命令硬编码。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public final class ZKCommandRegistry {

    /**
     * ZooKeeper 服务端支持的四字命令。
     *
     * <p>四字命令由 {@code org.apache.zookeeper.server.command.AbstractFourLetterCommand}
     * 和 {@code FourLetterCommands} 处理，不属于 {@link CliCommand} 体系，
     * 因此无法从 CliCommand 子类扫描结果推导，必须单独注册。</p>
     */
    private static final Set<String> FOUR_LETTER_WORD_COMMANDS = Set.of(
            "conf", "cons", "crst", "dirs", "dump", "envi", "kill", "mntr",
            "reqs", "ruok", "srst", "srvr", "stat", "wchc", "wchp", "wchs"
    );
    /** 全部动态命令元数据。 */
    private static final Map<String, List<ZKCommandDescriptor>> COMMANDS = loadCommands();
    /** 可实例化的 ZooKeeper CLI 命令类型。 */
    private static final Map<String, CliCommandType> CLI_COMMANDS = loadCliCommands();

    private ZKCommandRegistry() {
    }

    /**
     * 加载 ZooKeeper CLI 命令类型。
     *
     * @return CLI 命令类型映射
     */
    private static Map<String, CliCommandType> loadCliCommands() {
        Map<String, CliCommandType> commands = new LinkedHashMap<>();
        for (Map.Entry<String, CliCommand> entry : cliCommandMap().entrySet()) {
            CliCommand command = entry.getValue();
            commands.put(normalize(entry.getKey()), new CliCommandType(command.getCmdStr(), command.getClass()));
        }
        return Map.copyOf(commands);
    }

    /**
     * 加载 CLI、四字命令和终端辅助命令元数据。
     *
     * @return 命令元数据映射
     */
    private static Map<String, List<ZKCommandDescriptor>> loadCommands() {
        Map<String, List<ZKCommandDescriptor>> commands = new TreeMap<>();
        for (Map.Entry<String, CliCommand> entry : cliCommandMap().entrySet()) {
            CliCommand command = entry.getValue();
            String name = normalize(entry.getKey());
            addCommand(commands, new ZKCommandDescriptor(entry.getKey(),
                    name.equals("stat") ? "path" : command.getOptionStr(), ZKCommandDescriptor.Type.CLI));
        }
        for (String command : FOUR_LETTER_WORD_COMMANDS) {
            addCommand(commands, new ZKCommandDescriptor(command, "", ZKCommandDescriptor.Type.FOUR_LETTER_WORD));
        }
        addCommand(commands, new ZKCommandDescriptor("connect", "[-timeout timeout] [-server server] [-r]", ZKCommandDescriptor.Type.TERMINAL));
        addCommand(commands, new ZKCommandDescriptor("version", "", ZKCommandDescriptor.Type.TERMINAL));
        Map<String, List<ZKCommandDescriptor>> result = new TreeMap<>();
        commands.forEach((name, values) -> result.put(name, List.copyOf(values)));
        return Collections.unmodifiableMap(result);
    }

    /**
     * 添加命令元数据，同名命令可按参数形态保留多个实现。
     *
     * @param commands   命令索引
     * @param descriptor 命令元数据
     */
    private static void addCommand(Map<String, List<ZKCommandDescriptor>> commands,
                                   ZKCommandDescriptor descriptor) {
        commands.computeIfAbsent(normalize(descriptor.name()), key -> new java.util.ArrayList<>()).add(descriptor);
    }

    /**
     * 从 ZooKeeper 内置注册表和类路径中收集 CLI 命令。
     *
     * @return CLI 命令实例映射
     */
    @SuppressWarnings("unchecked")
    private static Map<String, CliCommand> cliCommandMap() {
        Map<String, CliCommand> commands = new LinkedHashMap<>();
        try {
            Field commandMap = ZooKeeperMain.class.getDeclaredField("commandMapCli");
            commandMap.setAccessible(true);
            commands.putAll((Map<String, CliCommand>) commandMap.get(null));
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
            scanJarCommands(commands, CliCommand.class.getPackageName().replace('.', '/'));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commands;
    }

    /**
     * 补充扫描 JAR 中的 ZooKeeper CLI 命令。
     *
     * @param commands  命令实例映射
     * @param packagePath CLI 包路径
     */
    private static void scanJarCommands(Map<String, CliCommand> commands, String packagePath) throws Exception {
        Enumeration<URL> resources = ZKCommandRegistry.class.getClassLoader().getResources(packagePath);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (!"jar".equals(resource.getProtocol())) {
                continue;
            }
            JarFile jarFile = ((JarURLConnection) resource.openConnection()).getJarFile();
            for (JarEntry entry : java.util.Collections.list(jarFile.entries())) {
                String name = entry.getName();
                if (entry.isDirectory() || !name.startsWith(packagePath + "/")
                        || !name.endsWith(".class") || name.contains("$")) {
                    continue;
                }
                addCliCommand(commands, name.substring(0, name.length() - 6).replace('/', '.'));
            }
        }
    }

    /**
     * 实例化 ZooKeeper CLI 命令类并加入命令映射。
     *
     * @param commands  命令实例映射
     * @param className 命令类名
     */
    private static void addCliCommand(Map<String, CliCommand> commands, String className) {
        try {
            Class<?> type = Class.forName(className);
            if (Modifier.isAbstract(type.getModifiers()) || !CliCommand.class.isAssignableFrom(type)) {
                return;
            }
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            CliCommand command = (CliCommand) constructor.newInstance();
            commands.putIfAbsent(command.getCmdStr(), command);
        } catch (Exception ex) {
            JulLog.warn("init CliCommand:{} fail", className, ex);
        }
    }

    /**
     * 查找命令元数据。
     *
     * @param command 命令名称
     * @return 命令元数据
     */
    public static Optional<ZKCommandDescriptor> find(String command) {
        List<ZKCommandDescriptor> descriptors = COMMANDS.get(normalize(command));
        if (descriptors == null || descriptors.isEmpty()) {
            return Optional.empty();
        }
        return descriptors.stream()
                .filter(descriptor -> descriptor.type() == ZKCommandDescriptor.Type.CLI)
                .findFirst()
                .or(() -> descriptors.stream().findFirst());
    }

    /**
     * 按完整参数形态查找命令。
     *
     * @param args 完整命令参数
     * @return 命令元数据
     */
    public static Optional<ZKCommandDescriptor> find(List<String> args) {
        if (args == null || args.isEmpty()) {
            return Optional.empty();
        }
        List<ZKCommandDescriptor> descriptors = COMMANDS.get(normalize(args.getFirst()));
        if (descriptors == null || descriptors.isEmpty()) {
            return Optional.empty();
        }
        if (descriptors.size() == 1) {
            return Optional.of(descriptors.getFirst());
        }
        ZKCommandDescriptor.Type expectedType = args.size() == 1
                ? ZKCommandDescriptor.Type.FOUR_LETTER_WORD
                : ZKCommandDescriptor.Type.CLI;
        return descriptors.stream()
                .filter(descriptor -> descriptor.type() == expectedType)
                .findFirst()
                .or(() -> descriptors.stream().findFirst());
    }

    /**
     * 查找同名命令的全部参数形态。
     *
     * @param command 命令名称
     * @return 命令元数据列表
     */
    public static List<ZKCommandDescriptor> findAll(String command) {
        return COMMANDS.getOrDefault(normalize(command), List.of());
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
        return COMMANDS.values().stream().flatMap(List::stream).toList();
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
                .flatMap(List::stream)
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
