package cn.oyzh.easyshell.terminal.zk2;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 为动态发现的 ZooKeeper 命令提供统一终端适配。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class ZKDynamicCommandHandler extends ZKTerminalCommandHandler<TerminalCommand> {

    /** 从命令用法中提取可选参数的表达式。 */
    private static final Pattern OPTION_PATTERN = Pattern.compile("\\[(-[^\\]\\s]+)]");

    /** 当前命令名称。 */
    private final String commandName;
    /** 当前命令元数据。 */
    private final ZKCommandDescriptor descriptor;

    /**
     * 使用已发现的命令元数据创建处理器。
     *
     * @param descriptor 命令元数据
     */
    public ZKDynamicCommandHandler(ZKCommandDescriptor descriptor) {
        this.commandName = descriptor.name();
        this.descriptor = descriptor;
    }

    /**
     * 按命令名称创建动态处理器。
     *
     * @param commandName 命令名称
     */
    public ZKDynamicCommandHandler(String commandName) {
        this.commandName = commandName;
        this.descriptor = ZKCommandRegistry.find(commandName).orElse(null);
    }

    /**
     * 使用 ZooKeeper 命令分词规则解析命令。
     */
    @Override
    public TerminalCommand parseCommand(String line) {
        List<String> words = ZKCommandTokenizer.tokenize(line);
        return this.parseCommand(line, words.toArray(String[]::new));
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        return terminal.executeCommand(this.commandArgs(command));
    }

    /**
     * 将命令名称和参数组合为 ZooKeeper CLI 参数。
     *
     * @param command 已解析命令
     * @return 完整命令参数
     */
    List<String> commandArgs(TerminalCommand command) {
        List<String> args = new ArrayList<>();
        args.add(command.getCommand());
        args.addAll(command.argsList());
        return args;
    }

    /**
     * 补全 ZooKeeper 节点路径或命令选项。
     */
    @Override
    public boolean completion(String line, ZKTerminalPane terminal) {
        List<String> words = ZKCommandTokenizer.tokenize(line);
        String last = words.isEmpty() ? "" : words.getLast();
        if (last.startsWith("/")) {
            return this.completePath(line, last, terminal);
        }
        return this.completeOption(line, last, terminal);
    }

    /**
     * 根据命令用法补全可选参数。
     */
    private boolean completeOption(String line, String input, ZKTerminalPane terminal) {
        if (descriptor == null || !input.startsWith("-")) {
            return false;
        }
        Matcher matcher = OPTION_PATTERN.matcher(descriptor.usage());
        List<String> options = new java.util.ArrayList<>();
        while (matcher.find()) {
            if (matcher.group(1).startsWith(input)) {
                options.add(matcher.group(1));
            }
        }
        if (options.isEmpty()) {
            return false;
        }
        if (options.size() == 1) {
            terminal.coverInput(line.substring(0, line.length() - input.length()) + options.getFirst());
        } else {
//            terminal.outputByPrompt(TextUtil.beautifyFormat(options, 4, 0));
            terminal.outputByPrompt(TextUtil.format(options));
            terminal.coverInput(line);
        }
        terminal.moveCaretEnd();
        return true;
    }

    /**
     * 查询父节点并补全 ZooKeeper 节点路径。
     */
    private boolean completePath(String line, String path, ZKTerminalPane terminal) {
        try {
            int slash = path.lastIndexOf('/');
            String parent = slash <= 0 ? "/" : path.substring(0, slash);
            List<String> matches = terminal.getClient().getChildren(parent).stream()
                    .map(child -> (parent.equals("/") ? "/" : parent + "/") + child)
                    .filter(child -> child.startsWith(path))
                    .sorted()
                    .toList();
            if (matches.isEmpty()) {
                return false;
            }
            if (matches.size() == 1) {
                terminal.coverInput(line.substring(0, line.length() - path.length()) + matches.getFirst());
            } else {
//                terminal.outputByPrompt(TextUtil.beautifyFormat(matches, 4, 0));
                terminal.outputByPrompt(TextUtil.format(matches));
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
        return commandName;
    }

    @Override
    public String commandArg() {
        return descriptor == null ? "" : descriptor.usage();
    }
}
