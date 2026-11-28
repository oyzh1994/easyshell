package cn.oyzh.easyshell.event.h2.view;

import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.event.Event;

/**
 * H2 视图结构变更事件。
 *
 * @since 2026-11-28
 */
public class ShellH2ViewChangedEvent extends Event<String> {

    private ShellH2Client client;

    private String schema;

    private String originalName;

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

    public String originalName() {
        return this.originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String viewName() {
        return this.data();
    }
}
