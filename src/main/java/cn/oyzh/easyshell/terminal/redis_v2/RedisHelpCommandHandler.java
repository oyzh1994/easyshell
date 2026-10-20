package cn.oyzh.easyshell.terminal.redis_v2;

import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;

/**
 * 展示动态发现的 Redis 命令帮助。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class RedisHelpCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, RedisTerminalPane terminal) {
        TerminalExecuteResult result = TerminalExecuteResult.ok();
        result.setResult(render(command == null ? "" : command.getContent(), terminal.registry(), terminal.lineEndingText()));
        return result;
    }

    /**
     * 渲染 Redis 命令帮助内容。
     *
     * @param input      帮助命令输入
     * @param registry   Redis 命令注册表
     * @param lineEnding 终端换行符
     * @return 帮助文本
     */
    public static String render(String input, RedisCommandRegistry registry, String lineEnding) {
        List<String> words = RedisCommandTokenizer.tokenize(input == null ? "" : input.replace("-?", " ").trim());
        if (!words.isEmpty() && words.getFirst().equalsIgnoreCase("help")) {
            words = words.subList(1, words.size());
        }
        if (words.isEmpty()) {
            return String.join(lineEnding, registry.commandNames());
        }
        RedisCommandDescriptor command = registry.find(words);
        if (command == null) {
            return "Unknown command: " + String.join(" ", words);
        }
        StringBuilder builder = new StringBuilder(command.usage());
        if (command.summary() != null && !command.summary().isBlank()) {
            builder.append(lineEnding).append(command.summary());
        }
        if (!command.flags().isEmpty()) {
            builder.append(lineEnding).append("flags: ").append(String.join(", ", command.flags()));
        }
        if (!command.subcommands().isEmpty()) {
            builder.append(lineEnding).append("subcommands: ");
            builder.append(String.join(", ", command.subcommands().values().stream()
                    .map(RedisCommandDescriptor::name).sorted().toList()));
        }
        return builder.toString();
    }

    @Override
    public String commandName() {
        return "help";
    }

    @Override
    public String commandArg() {
        return "[command]";
    }

    @Override
    public String commandDesc() {
        return "Show dynamically discovered Redis commands";
    }
}
