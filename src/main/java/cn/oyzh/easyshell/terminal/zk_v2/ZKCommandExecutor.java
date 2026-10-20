package cn.oyzh.easyshell.terminal.zk_v2;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Version;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.client.FourLetterWordMain;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 执行动态发现的 ZooKeeper 命令。
 */
public class ZKCommandExecutor {

    public String execute(ZKCommandDescriptor descriptor, List<String> args, ZooKeeper zooKeeper,
                          String host, int port) throws Exception {
        if (descriptor == null) {
            throw new IllegalArgumentException("Unknown ZooKeeper command");
        }
        return switch (descriptor.type()) {
            case CLI -> executeCli(descriptor.name(), args, zooKeeper);
            case FOUR_LETTER_WORD -> FourLetterWordMain.send4LetterWord(host, port,
                    descriptor.name().equals("stat4") ? "stat" : descriptor.name());
            case TERMINAL -> executeTerminal(descriptor.name());
        };
    }

    private String executeTerminal(String commandName) {
        if (commandName.equalsIgnoreCase("version")) {
            return "ZooKeeper CLI version: " + Version.getFullVersion();
        }
        throw new IllegalArgumentException("Terminal command must be handled by the terminal: " + commandName);
    }

    private String executeCli(String commandName, List<String> args, ZooKeeper zooKeeper) throws Exception {
        ZKCommandRegistry.CliCommandType commandType = ZKCommandRegistry.cliCommandType(commandName);
        if (commandType == null) {
            throw new IllegalArgumentException("Unknown ZooKeeper CLI command: " + commandName);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream printStream = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            CliCommand command = commandType.newCommand();
            command.setZk(zooKeeper);
            command.setOut(printStream);
            command.setErr(printStream);
            command.parse(args.toArray(String[]::new));
            command.exec();
        }
        return output.toString(StandardCharsets.UTF_8);
    }

}
