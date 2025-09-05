package cn.oyzh.easyshell.event.redis;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/12/11
 */
public class ShellRedisKeyTTLUpdatedEvent extends Event<ShellConnect> implements  EventFormatter {

    private Long ttl;

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    private String key;

    private int dbIndex;

    @Override
    public String eventFormat() {
        return String.format(
                "[%s] "+ I18nHelper.ttlUpdated() +"[%s-db%s] ttl:%s",
                this.data().getName(), this.key, this.dbIndex, this.ttl
        );
    }
}
