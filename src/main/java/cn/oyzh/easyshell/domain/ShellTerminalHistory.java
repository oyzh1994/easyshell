package cn.oyzh.easyshell.domain;

import cn.oyzh.fx.terminal.histroy.TerminalHistory;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

/**
 * @author oyzh
 * @since 2024-11-25
 */
@Table("t_terminal_history")
public class ShellTerminalHistory extends TerminalHistory {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String tid;

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }
}
