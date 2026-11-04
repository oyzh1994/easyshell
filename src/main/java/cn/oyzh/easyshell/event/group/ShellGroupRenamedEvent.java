package cn.oyzh.easyshell.event.group;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.easyshell.util.ShellI18nHelper;

/**
 * 分组已更名事件
 *
 * @author oyzh
 * @since 2025-03-03
 */
public class ShellGroupRenamedEvent extends Event<String> implements EventFormatter {

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    /**
     * 旧名称
     */
    private String oldName;

    @Override
    public String eventFormat() {
return ShellI18nHelper.eventGroupRenamed(this.data(), this.oldName);
    }
}
