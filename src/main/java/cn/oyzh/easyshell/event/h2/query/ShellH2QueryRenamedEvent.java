package cn.oyzh.easyshell.event.h2.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.event.Event;

/**
 * H2 查询重命名事件。
 *
 * @since 2026-11-28
 */
public class ShellH2QueryRenamedEvent extends Event<ShellQuery> {

    private ShellH2Client client;

    private String oldName;

    public ShellH2Client client() {
        return this.client;
    }

    public void setClient(ShellH2Client client) {
        this.client = client;
    }

    public String oldName() {
        return this.oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String queryId() {
        return this.data().getUid();
    }

    public String queryName() {
        return this.data().getName();
    }
}
