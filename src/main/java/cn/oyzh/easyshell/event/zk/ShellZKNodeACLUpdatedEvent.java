package cn.oyzh.easyshell.event.zk;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/24
 */
public class ShellZKNodeACLUpdatedEvent extends Event<ShellConnect> implements EventFormatter {

    private String nodePath;

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s acl updated, path:%s] ", I18nHelper.connect(), this.data().getName(), this.nodePath);
    }
}
