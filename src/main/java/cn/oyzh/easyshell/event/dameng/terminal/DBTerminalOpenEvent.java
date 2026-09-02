package cn.oyzh.easyshell.event.dameng.terminal;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2023/11/20
 */
public class DBTerminalOpenEvent extends Event<ShellDamengClient> {

    private String schema;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
