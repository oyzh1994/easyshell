package cn.oyzh.easyshell.event.h2.terminal;

import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.event.Event;

/**
 * H2 终端打开事件。
 *
 * @since 2026-11-28
 */
public class ShellH2TerminalOpenEvent extends Event<ShellH2Client> {

    private String schema;

    public ShellH2Client client() {
        return this.data();
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
