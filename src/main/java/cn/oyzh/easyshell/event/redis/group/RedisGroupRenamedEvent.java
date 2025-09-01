package cn.oyzh.easyshell.event.redis.group;

import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class RedisGroupRenamedEvent extends Event<String> implements EventFormatter {

    private String oldName;

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    @Override
    public String eventFormat() {
        return String.format("[%s:%s renamed from %s] ", I18nHelper.group(), this.data(), this.oldName);
    }
}
