package cn.oyzh.easyshell.event.redis;

import cn.oyzh.easyshell.trees.redis.RedisListKeyTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellRedisListRowAddedEvent extends Event<RedisListKeyTreeItem> implements EventFormatter {

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    private String member;

    @Override
    public String eventFormat() {
        return String.format(
                "[%s] " + I18nHelper.key() + ":%s(db%s) " + I18nHelper.rowAdded() + ":%s",
                this.data().infoName(), this.key, this.data().dbIndex(), this.member
        );
    }
}
