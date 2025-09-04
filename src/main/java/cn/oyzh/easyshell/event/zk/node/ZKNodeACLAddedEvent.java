package cn.oyzh.easyshell.event.zk.node;

import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/24
 */
public class ZKNodeACLAddedEvent extends Event<ZKConnect> implements EventFormatter {

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    private String nodePath;

    @Override
    public String eventFormat() {
        return String.format("[%s:%s acl added, path:%s] ", I18nHelper.connect(), this.data().getName(), this.nodePath);
    }

}
