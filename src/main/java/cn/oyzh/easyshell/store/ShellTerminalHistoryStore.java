package cn.oyzh.easyshell.store;

import cn.oyzh.easyshell.domain.ShellTerminalHistory;
import cn.oyzh.store.jdbc.JdbcStandardStore;

/**
 * shell终端历史存储
 *
 * @author oyzh
 * @since 2024-11-25
 */
public class ShellTerminalHistoryStore extends JdbcStandardStore<ShellTerminalHistory> {

    /**
     * 当前实例
     */
    public static final ShellTerminalHistoryStore INSTANCE = new ShellTerminalHistoryStore();

    /**
     * 替换
     *
     * @param model 模型
     * @return 结果
     */
    public boolean replace(ShellTerminalHistory model) {
        return this.insert(model);
    }

    @Override
    protected Class<ShellTerminalHistory> modelClass() {
        return ShellTerminalHistory.class;
    }
}
