package cn.oyzh.easyshell.event.redis.key;

import cn.oyzh.easyshell.trees.redis.RedisSetKeyTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellRedisSetMemberAddedEvent extends Event<RedisSetKeyTreeItem> implements EventFormatter {

    private String key;

    private String member;

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

    @Override
    public String eventFormat() {
        return String.format(
                "[%s] " + I18nHelper.key() + ":%s(db%s) " + I18nHelper.memberAdded() + ":%s",
                this.data().infoName(), this.key, this.data().dbIndex(), this.member
        );
    }

}
