package cn.oyzh.easyshell.event.client;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;

/**
 * @author oyzh
 * @since 2024-12-20
 */
public class ShellClientActionEvent extends Event<String> implements EventFormatter {

    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String eventFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.data());
        sb.append(" > ");
        sb.append(this.action);
        return sb.toString();
    }
}
