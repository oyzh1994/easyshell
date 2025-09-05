package cn.oyzh.easyshell.event.zk;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ShellZKNodeDeletedEvent extends Event<String> implements EventFormatter {

    public String getConnectName() {
        return connectName;
    }

    public void setConnectName(String connectName) {
        this.connectName = connectName;
    }

    public boolean isDelChildren() {
        return delChildren;
    }

    public void setDelChildren(boolean delChildren) {
        this.delChildren = delChildren;
    }

    private String connectName;

    private boolean delChildren;

    @Override
    public String eventFormat() {
        return String.format("[%s:%s deleted, path:%s] ", I18nHelper.connect(), this.connectName, this.data());
    }
}
