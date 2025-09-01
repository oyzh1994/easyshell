package cn.oyzh.easyshell.terminal.redis;

import cn.oyzh.fx.terminal.histroy.TerminalHistory;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

/**
 * @author oyzh
 * @since 2024-11-29
 */
@Table("t_terminal_history")
public class RedisTerminalHistory extends TerminalHistory {

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String tid;
}
