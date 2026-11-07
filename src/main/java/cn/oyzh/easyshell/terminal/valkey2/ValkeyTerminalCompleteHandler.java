package cn.oyzh.easyshell.terminal.valkey2;

import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.complete.BaseTerminalCompleteHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Valkey v2 动态命令补全。
 *
 * @author oyzh
 * @since 2026-10-20
 */
public class ValkeyTerminalCompleteHandler extends BaseTerminalCompleteHandler<ValkeyTerminalPane> {

    /** 当前实例。 */
    public static final ValkeyTerminalCompleteHandler INSTANCE = new ValkeyTerminalCompleteHandler();

    /**
     * 查找可补全的静态和动态 Valkey 命令。
     */
    @Override
    protected List<TerminalCommandHandler<?, ?>> findCommandHandlers(ValkeyTerminalPane terminal, String line) {
        List<String> words = ValkeyCommandTokenizer.tokenize(line);
        boolean commandOnly = line.isBlank() || (!line.trim().contains(" ") && !line.endsWith(" "));
        String prefix = commandOnly ? line.trim() : words.getFirst();
        Map<String, TerminalCommandHandler<?, ?>> handlers = new LinkedHashMap<>();
        for (TerminalCommandHandler<?, ?> handler : TerminalManager.listHandler(terminal.terminalName())) {
            if (handler.commandName().toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
                handlers.put(handler.commandFullName().toLowerCase(Locale.ROOT), handler);
            }
        }
        if (commandOnly) {
            for (ValkeyCommandDescriptor descriptor : terminal.registry().complete(List.of(prefix))) {
                handlers.putIfAbsent(descriptor.displayName().toLowerCase(Locale.ROOT),
                        new ValkeyDynamicCommandHandler(descriptor));
            }
        } else {
            List<String> completionWords = new java.util.ArrayList<>(words);
            if (line.endsWith(" ")) {
                completionWords.add("");
            }
            List<ValkeyCommandDescriptor> subcommands = terminal.registry().complete(completionWords);
            if (!subcommands.isEmpty()) {
                List<TerminalCommandHandler<?, ?>> result = new ArrayList<>();
                subcommands.forEach(command -> result.add(new ValkeyDynamicCommandHandler(command)));
                return result;
            }
            ValkeyCommandDescriptor descriptor = terminal.registry().find(words);
            return List.of(descriptor == null
                    ? new ValkeyDynamicCommandHandler(prefix)
                    : new ValkeyDynamicCommandHandler(descriptor));
        }
        List<TerminalCommandHandler<?, ?>> result = new ArrayList<>(handlers.values());
        result.sort(Comparator.comparing(TerminalCommandHandler::commandFullName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }
}
