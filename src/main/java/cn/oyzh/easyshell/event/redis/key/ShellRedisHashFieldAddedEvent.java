package cn.oyzh.easyshell.event.redis.key;

import cn.oyzh.easyshell.trees.redis.RedisHashKeyTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellRedisHashFieldAddedEvent extends Event<RedisHashKeyTreeItem> implements EventFormatter {
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String key;

    private String field;

    private String value;

    @Override
    public String eventFormat() {
        return String.format(
                "[%s] " + I18nHelper.key() + ":%s(db%s) " + I18nHelper.addField() + ":%s " + I18nHelper.value() + ":%s",
                this.data().infoName(), this.key, this.data().dbIndex(), this.field, this.value
        );
    }
}
