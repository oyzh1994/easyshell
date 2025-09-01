package cn.oyzh.easyshell.event.redis.key;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.trees.redis.RedisKeyTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/12/12
 */
public class RedisKeyMovedEvent extends Event<RedisKeyTreeItem> implements EventFormatter {
    public int getTargetDB() {
        return targetDB;
    }

    public void setTargetDB(int targetDB) {
        this.targetDB = targetDB;
    }

    private int targetDB;

    public int sourceDB() {
        return this.data().dbIndex();
    }

    public ShellConnect redisConnect() {
        return this.data().redisConnect();
    }

    @Override
    public String eventFormat() {
        return String.format(
                "[%s] " + I18nHelper.keyMoved() + "[%s-db%s] " + I18nHelper.targetDatabase() + ":%s",
                this.redisConnect().getName(), this.data().key(), this.data().dbIndex(), this.targetDB
        );
    }
}
