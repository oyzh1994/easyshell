package cn.oyzh.easyshell.terminal.zk_v2;

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
 * ZooKeeper v2 终端命令补全。
 */
public class ZKTerminalCompleteHandler extends BaseTerminalCompleteHandler<ZKTerminalPane> {

    public static final ZKTerminalCompleteHandler INSTANCE = new ZKTerminalCompleteHandler();

    @Override
    protected List<TerminalCommandHandler<?, ?>> findCommandHandlers(ZKTerminalPane terminal, String line) {
        List<String> words = ZKCommandTokenizer.tokenize(line);
        String commandName = words.isEmpty() ? "" : words.getFirst();
        boolean completeCommand = line.isBlank() || !line.trim().contains(" ");
        String prefix = completeCommand ? line.trim() : commandName;

        Map<String, TerminalCommandHandler<?, ?>> handlers = new LinkedHashMap<>();
        for (TerminalCommandHandler<?, ?> handler : TerminalManager.listHandler(terminal.terminalName())) {
            if (handler.commandName().toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) {
                handlers.put(handler.commandFullName().toLowerCase(Locale.ROOT), handler);
            }
        }
        if (completeCommand) {
            for (ZKCommandDescriptor descriptor : ZKCommandRegistry.findCommands(prefix)) {
                handlers.putIfAbsent(descriptor.name().toLowerCase(Locale.ROOT), new ZKDynamicCommandHandler(descriptor));
            }
        } else {
            TerminalCommandHandler<?, ?> exactHandler = TerminalManager.findHandler(terminal.terminalName(), commandName);
            if (exactHandler != null) {
                return List.of(exactHandler);
            }
            if (ZKCommandRegistry.contains(commandName)) {
                return List.of(new ZKDynamicCommandHandler(commandName));
            }
        }

        List<TerminalCommandHandler<?, ?>> result = new ArrayList<>(handlers.values());
        result.sort(Comparator.comparing(TerminalCommandHandler::commandFullName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }
}
