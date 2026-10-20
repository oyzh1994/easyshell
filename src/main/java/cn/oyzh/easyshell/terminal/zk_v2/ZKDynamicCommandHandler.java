package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 为动态发现的 ZooKeeper 命令提供统一终端适配。
 */
public class ZKDynamicCommandHandler extends ZKTerminalCommandHandler<TerminalCommand> {

    private static final Pattern OPTION_PATTERN = Pattern.compile("\\[(-[^\\]\\s]+)]");

    private final String commandName;
    private final ZKCommandDescriptor descriptor;

    public ZKDynamicCommandHandler(ZKCommandDescriptor descriptor) {
        this.commandName = descriptor.name();
        this.descriptor = descriptor;
    }

    public ZKDynamicCommandHandler(String commandName) {
        this.commandName = commandName;
        this.descriptor = ZKCommandRegistry.find(commandName).orElse(null);
    }

    @Override
    public TerminalCommand parseCommand(String line) {
        List<String> words = ZKCommandTokenizer.tokenize(line);
        return this.parseCommand(line, words.toArray(String[]::new));
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        return terminal.executeCommand(List.of(command.getArgs()));
    }

    @Override
    public boolean completion(String line, ZKTerminalPane terminal) {
        List<String> words = ZKCommandTokenizer.tokenize(line);
        String last = words.isEmpty() ? "" : words.getLast();
        if (last.startsWith("/")) {
            return this.completePath(line, last, terminal);
        }
        return this.completeOption(line, last, terminal);
    }

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
