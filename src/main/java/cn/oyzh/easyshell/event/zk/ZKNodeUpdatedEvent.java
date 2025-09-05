package cn.oyzh.easyshell.event.zk;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ZKNodeUpdatedEvent extends Event<String> implements EventFormatter {

    public String getConnectName() {
        return connectName;
    }

    public void setConnectName(String connectName) {
        this.connectName = connectName;
    }

    private String connectName;

    @Override
    public String eventFormat() {
        return String.format("[%s] " + I18nHelper.nodeUpdated() + ":%s", this.connectName, this.data());
    }
}
