package cn.oyzh.easyshell.terminal.redis;


import cn.oyzh.fx.terminal.histroy.BaseTerminalHistoryHandler;
import cn.oyzh.fx.terminal.histroy.TerminalHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/8/28
 */
public class RedisTerminalHistoryHandler extends BaseTerminalHistoryHandler {

    /**
     * 当前实例
     */
    public static final RedisTerminalHistoryHandler INSTANCE = new RedisTerminalHistoryHandler();

    /**
     * 缓存记录
     */
    private final List<RedisTerminalHistory> cecheList = new ArrayList<>(24);

    /**
     * 存储器
     */
    private final RedisTerminalHistoryStore historyStore = RedisTerminalHistoryStore.INSTANCE;

    @Override
    public void clearHistory() {
        this.historyStore.clear();
        this.cecheList.clear();
    }

    @Override
    public List<RedisTerminalHistory> listHistory() {
        if (this.cecheList.isEmpty()) {
            this.cecheList.addAll(this.historyStore.selectList());
        }
        return this.cecheList;
    }

    @Override
    public void addHistory(TerminalHistory history) {
        RedisTerminalHistory terminalHistory = new RedisTerminalHistory();
        terminalHistory.setSaveTime(System.currentTimeMillis());
        terminalHistory.setLine(history.getLine());
        this.historyStore.insert(terminalHistory);
        this.cecheList.add(terminalHistory);
    }
}
