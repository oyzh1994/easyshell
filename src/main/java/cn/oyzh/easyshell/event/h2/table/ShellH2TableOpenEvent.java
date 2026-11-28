package cn.oyzh.easyshell.event.h2.table;

import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.event.Event;

/**
 * H2 表打开事件。
 *
 * @since 2026-11-28
 */
public class ShellH2TableOpenEvent extends Event<String> {

    private ShellH2Client client;

    private String schema;

    public ShellH2Client client() {
        return this.client;
    }

    public void setClient(ShellH2Client client) {
        this.client = client;
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String tableName() {
        return this.data();
    }
}
