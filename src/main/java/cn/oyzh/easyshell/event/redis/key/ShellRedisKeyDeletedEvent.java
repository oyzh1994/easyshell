package cn.oyzh.easyshell.event.redis.key;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellRedisKeyDeletedEvent extends Event<ShellConnect> implements  EventFormatter {

    private String key;

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

    private int dbIndex;

    @Override
    public String eventFormat() {
        return String.format(
                "[%s] [%s-db%s]"+ I18nHelper.keyDeleted() ,
                this.data().getName(), this.key, this.dbIndex
        );
    }
}
