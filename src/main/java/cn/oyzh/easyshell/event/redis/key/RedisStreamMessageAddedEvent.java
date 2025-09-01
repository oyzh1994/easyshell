package cn.oyzh.easyshell.event.redis.key;

import cn.oyzh.easyshell.trees.redis.key.RedisStreamKeyTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/11/20
 */
public class RedisStreamMessageAddedEvent extends Event<RedisStreamKeyTreeItem> implements EventFormatter {

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    @Override
    public String eventFormat() {
        return String.format(
                "[%s] " + I18nHelper.key() + ":%s(db%s) "+ I18nHelper.messageAdded() +":%s",
                this.data().infoName(), this.key, this.data().dbIndex(), this.message
        );
    }
}
