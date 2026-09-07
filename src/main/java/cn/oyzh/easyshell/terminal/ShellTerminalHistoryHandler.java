package cn.oyzh.easyshell.terminal;

import cn.oyzh.easyshell.domain.ShellTerminalHistory;
import cn.oyzh.easyshell.store.ShellTerminalHistoryStore;
import cn.oyzh.fx.terminal.histroy.BaseTerminalHistoryHandler;
import cn.oyzh.fx.terminal.histroy.TerminalHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/8/28
 */
public class ShellTerminalHistoryHandler extends BaseTerminalHistoryHandler {

    public static final ShellTerminalHistoryHandler INSTANCE = new ShellTerminalHistoryHandler();

    private final List<ShellTerminalHistory> cacheList = new ArrayList<>(24);

    private final ShellTerminalHistoryStore historyStore = ShellTerminalHistoryStore.INSTANCE;

    @Override
    public void clearHistory() {
        this.historyStore.clear();
        this.cacheList.clear();
    }

    @Override
    public List<ShellTerminalHistory> listHistory() {
        if (this.cacheList.isEmpty()) {
            this.cacheList.addAll(this.historyStore.selectList());
        }
        return this.cacheList;
    }

    @Override
    public void addHistory(TerminalHistory history) {
        ShellTerminalHistory terminalHistory = new ShellTerminalHistory();
        terminalHistory.setSaveTime(System.currentTimeMillis());
        terminalHistory.setLine(history.getLine());
        this.historyStore.insert(terminalHistory);
        this.cacheList.add(terminalHistory);
    }
}
