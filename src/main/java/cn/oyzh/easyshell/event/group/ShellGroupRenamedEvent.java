package cn.oyzh.easyshell.event.group;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

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
        return String.format("[%s:%s renamed from %s] ", I18nHelper.folder(), this.data(), this.oldName);
    }
}
