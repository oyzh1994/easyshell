package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;

/**
 * ZooKeeper v2 终端帮助命令。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class ZKHelpCommandHandler extends ZKTerminalCommandHandler<TerminalCommand> {

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        result.setResult(render(command == null ? "" : command.getContent()));
        return result;
    }

    /**
     * 渲染 ZooKeeper 命令帮助内容。
     *
     * @param input help 命令输入
     * @return 帮助文本
     */
    public static String render(String input) {
        if (input == null) {
            return "";
        }
        List<String> words = ZKCommandTokenizer.tokenize(input.replace("-?", " ").trim());
        if (words.isEmpty() || words.getFirst().equalsIgnoreCase("help")) {
            return words.size() < 2 ? "" : words.get(1);
        }
        String commandName = words.getFirst();
        if (commandName.isEmpty()) {
            return "";
        }
        return ZKCommandRegistry.find(commandName).map(command -> command.name() + (command.usage().isBlank() ? "" : " " + command.usage())).orElse("Unknown command: " + commandName);
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
