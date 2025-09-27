package cn.oyzh.easyshell.terminal.zk;

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
public class ZKTerminalHistoryHandler extends BaseTerminalHistoryHandler {

    /**
     * 当前实例
     */
    public static final ZKTerminalHistoryHandler INSTANCE = new ZKTerminalHistoryHandler();

    /**
     * 缓存记录
     */
    private final List<ShellTerminalHistory> cecheList = new ArrayList<>(24);

    /**
     * 存储器
     */
    private final ShellTerminalHistoryStore historyStore = ShellTerminalHistoryStore.INSTANCE;

    @Override
    public void clearHistory() {
        this.historyStore.clear();
        this.cecheList.clear();
    }

    @Override
    public List<ShellTerminalHistory> listHistory() {
        if (this.cecheList.isEmpty()) {
            this.cecheList.addAll(this.historyStore.selectList());
        }
        return this.cecheList;
    }

    @Override
    public void addHistory(TerminalHistory history) {
        ShellTerminalHistory terminalHistory = new ShellTerminalHistory();
        terminalHistory.setSaveTime(System.currentTimeMillis());
        terminalHistory.setLine(history.getLine());
        this.historyStore.insert(terminalHistory);
        this.cecheList.add(terminalHistory);
    }
}
