package cn.oyzh.easyshell.event.redis.key;

import cn.oyzh.easyshell.trees.redis.key.RedisKeyTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/12/11
 */
public class RedisKeyRenamedEvent extends Event<RedisKeyTreeItem> implements EventFormatter {

    private String oldKey;

    public String getOldKey() {
        return oldKey;
    }

    public void setOldKey(String oldKey) {
        this.oldKey = oldKey;
    }

    @Override
    public String eventFormat() {
        return String.format(
                "[%s] " + I18nHelper.keyRenamed() + "[%s-db%s] " + I18nHelper.newName() + ":%s",
                this.data().shellConnect().getName(), this.oldKey, this.data().dbIndex(), this.data().key()
        );
    }
}
