package cn.oyzh.easyshell.terminal.redis2;

import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态 Redis 命令处理器。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class RedisDynamicCommandHandler extends RedisTerminalCommandHandler<TerminalCommand> {

    /** 当前动态命令名称。 */
    private final String commandName;
    /** 当前动态命令元数据。 */
    private final RedisCommandDescriptor descriptor;

    /**
     * 使用已发现的命令元数据创建处理器。
     *
     * @param descriptor 命令元数据
     */
    public RedisDynamicCommandHandler(RedisCommandDescriptor descriptor) {
        this.commandName = descriptor.displayName();
        this.descriptor = descriptor;
    }

    /**
     * 创建仅用于查询或错误提示的动态处理器。
     *
     * @param commandName 命令名称
     */
    public RedisDynamicCommandHandler(String commandName) {
        this.commandName = commandName;
        this.descriptor = null;
    }

    /**
     * 将完整 Redis 命令参数交给终端执行。
     *
     * @param command  已解析命令
     * @param terminal Redis 终端
     * @return 执行结果
     */
    @Override
    public TerminalExecuteResult execute(TerminalCommand command, RedisTerminalPane terminal) {
        List<String> args = new ArrayList<>();
        args.add(command.getCommand());
        args.addAll(command.argsList());
        return terminal.executeCommand(args);
    }

    /**
     * 根据命令元数据补全 Redis 键名。
     *
     * @param line     当前输入
     * @param terminal Redis 终端
     * @return 是否已处理补全
     */
    @Override
    public boolean completion(String line, RedisTerminalPane terminal) {
        List<String> words = RedisCommandTokenizer.tokenize(line);
        boolean trailingSpace = line.endsWith(" ");
        if (words.isEmpty() || (!trailingSpace && words.size() < 2)) {
            return false;
        }
        List<String> commandWords = trailingSpace ? words : words.subList(0, words.size() - 1);
        String prefix = trailingSpace ? "" : words.getLast();
        RedisCommandDescriptor current = terminal.registry().find(commandWords);
        if (current == null || current.firstKey() <= 0) {
            return false;
        }
        try {
            List<String> keys = terminal.getClient().allKeys(terminal.getDbIndex(), prefix + "*").stream().sorted().toList();
            if (keys.isEmpty()) {
                return false;
            }
            if (keys.size() == 1) {
                terminal.coverInput(line.substring(0, line.length() - prefix.length()) + keys.getFirst());
            } else {
                terminal.outputByPrompt(String.join(terminal.lineEndingText(), keys));
                terminal.coverInput(line);
            }
            terminal.moveCaretEnd();
            return true;
        } catch (Exception ex) {
            terminal.onError(ex);
            return false;
        }
    }

    @Override
    public String commandName() {
        return this.commandName;
    }

    @Override
    public String commandArg() {
        return this.descriptor == null ? "" : this.descriptor.usage().substring(this.descriptor.displayName().length());
    }

    @Override
    public String commandDesc() {
        return this.descriptor == null || this.descriptor.summary() == null ? "" : this.descriptor.summary();
    }

}
