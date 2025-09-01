package cn.oyzh.easyshell.event.redis.key;

import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.easyshell.trees.redis.RedisStringKeyTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/11/20
 */
public class RedisHyLogElementsAddedEvent extends Event<RedisStringKeyTreeItem> implements EventFormatter {
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String[] getElements() {
        return elements;
    }

    public void setElements(String[] elements) {
        this.elements = elements;
    }

    private String key;

    private String[] elements;

    @Override
    public String eventFormat() {
        return String.format(
                "[%s] " + I18nHelper.key() + ":%s(db%s) " + I18nHelper.addElement() + ":%s",
                this.data().infoName(), this.key, this.data().dbIndex(), ArrayUtil.toString(this.elements)
        );
    }
}
