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
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class ZKCommandExecutor {

    /**
     * 执行 ZooKeeper 命令并返回标准输出。
     *
     * @param descriptor 命令元数据
     * @param args       完整命令参数
     * @param zooKeeper  ZooKeeper 客户端
     * @param host       ZooKeeper 服务地址
     * @param port       ZooKeeper 服务端口
     * @return 命令输出
     * @throws Exception 命令执行失败
     */
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

    /**
     * 执行由终端提供的辅助命令。
     *
     * @param commandName 命令名称
     * @return 命令输出
     */
    private String executeTerminal(String commandName) {
        if (commandName.equalsIgnoreCase("version")) {
            return "ZooKeeper CLI version: " + Version.getFullVersion();
        }
        throw new IllegalArgumentException("Terminal command must be handled by the terminal: " + commandName);
    }

    /**
     * 通过 ZooKeeper CLI 执行命令。
     *
     * @param commandName 命令名称
     * @param args        完整命令参数
     * @param zooKeeper   ZooKeeper 客户端
     * @return 命令输出
     * @throws Exception 命令执行失败
     */
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
