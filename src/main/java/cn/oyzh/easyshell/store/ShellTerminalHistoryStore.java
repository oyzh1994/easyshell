package cn.oyzh.easyshell.store;

import cn.oyzh.easyshell.domain.ShellTerminalHistory;
import cn.oyzh.store.jdbc.JdbcStandardStore;

/**
 * @author oyzh
 * @since 2024-11-25
 */
public class ShellTerminalHistoryStore extends JdbcStandardStore<ShellTerminalHistory> {

    /**
     * 当前实例
     */
    public static final ShellTerminalHistoryStore INSTANCE = new ShellTerminalHistoryStore();

    public boolean replace(ShellTerminalHistory model) {
        return this.insert(model);
    }

    @Override
    protected Class<ShellTerminalHistory> modelClass() {
        return ShellTerminalHistory.class;
    }
}
