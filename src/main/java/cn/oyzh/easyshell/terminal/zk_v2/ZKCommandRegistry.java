package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ClassUtil;
import org.apache.zookeeper.ZooKeeperMain;
import org.apache.zookeeper.cli.CliCommand;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * 动态发现 ZooKeeper CLI 命令，避免逐命令硬编码。
 */
public final class ZKCommandRegistry {

    private static final Set<String> FOUR_LETTER_WORD_COMMANDS = Set.of(
            "conf", "cons", "crst", "dirs", "dump", "envi", "kill", "mntr",
            "reqs", "ruok", "srst", "srvr", "stat4", "wchc", "wchp", "wchs"
    );
    private static final Map<String, String> LEGACY_CLI_ALIASES = Map.of(
            "ls2", "ls",
            "rmr", "deleteall"
    );

    private static final Map<String, ZKCommandDescriptor> COMMANDS = loadCommands();
    private static final Map<String, CliCommandType> CLI_COMMANDS = loadCliCommands();

    private ZKCommandRegistry() {
    }

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

    public static Optional<ZKCommandDescriptor> find(String command) {
        return Optional.ofNullable(COMMANDS.get(normalize(command)));
    }

    public static boolean contains(String command) {
        return COMMANDS.containsKey(normalize(command));
    }

    public static Collection<ZKCommandDescriptor> commands() {
        return COMMANDS.values();
    }

    public static Set<String> commandNames() {
        return COMMANDS.keySet();
    }

    public static List<ZKCommandDescriptor> findCommands(String prefix) {
        String normalizedPrefix = normalize(prefix);
        return COMMANDS.values().stream()
                .filter(command -> normalize(command.name()).startsWith(normalizedPrefix))
                .toList();
    }

    public static CliCommandType cliCommandType(String command) {
        return CLI_COMMANDS.get(normalize(command));
    }

    private static String normalize(String command) {
        return command == null ? "" : command.toLowerCase(Locale.ROOT);
    }

    /**
     * 保存 ZooKeeper 枚举并按需创建新命令实例。
     */
    public record CliCommandType(String commandName, Class<? extends CliCommand> commandClass) {

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
