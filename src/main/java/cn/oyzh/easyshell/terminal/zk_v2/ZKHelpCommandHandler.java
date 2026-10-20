package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ZooKeeper v2 终端帮助命令。
 */
public class ZKHelpCommandHandler extends ZKTerminalCommandHandler<TerminalCommand> {

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        result.setResult(render(command == null ? "" : command.getContent()));
        return result;
    }

    public static String render(String input) {
        String commandName = commandName(input);
        if (!commandName.isEmpty()) {
            return ZKCommandRegistry.find(commandName)
                    .map(command -> command.name() + (command.usage().isBlank() ? "" : " " + command.usage()))
                    .orElse("Unknown command: " + commandName);
        }

        List<String> commands = new ArrayList<>();
        commands.add("help");
        commands.add("clear");
        ZKCommandRegistry.commands().stream()
                .sorted(Comparator.comparing(command -> command.name().toLowerCase()))
                .forEach(command -> commands.add(command.name() + (command.usage().isBlank() ? "" : " " + command.usage())));
        return String.join(System.lineSeparator(), commands);
    }

    private static String commandName(String input) {
        if (input == null) {
            return "";
        }
        List<String> words = ZKCommandTokenizer.tokenize(input.replace("-?", " ").trim());
        if (words.isEmpty() || words.getFirst().equalsIgnoreCase("help")) {
            return words.size() < 2 ? "" : words.get(1);
        }
        return words.getFirst();
    }

    @Override
    public String commandName() {
        return "help";
    }

    @Override
    public String commandDesc() {
        return "Show ZooKeeper terminal commands";
    }
}
