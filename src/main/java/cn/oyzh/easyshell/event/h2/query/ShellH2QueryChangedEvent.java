package cn.oyzh.easyshell.event.h2.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.event.Event;

/**
 * H2 查询保存事件。
 *
 * @since 2026-11-28
 */
public class ShellH2QueryChangedEvent extends Event<ShellQuery> {

    private ShellH2Client client;

    public ShellH2Client client() {
        return this.client;
    }

    public void setClient(ShellH2Client client) {
        this.client = client;
    }
}
