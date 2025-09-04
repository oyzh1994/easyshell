package cn.oyzh.easyshell.event.redis.key;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * @author oyzh
 * @since 2023/12/12
 */
public class ShellRedisKeyCopiedEvent extends Event<List<String>> implements EventFormatter {

    private int dbIndex;

    private int targetDB;

    private ShellConnect connect;

    public int getTargetDB() {
        return targetDB;
    }

    public void setTargetDB(int targetDB) {
        this.targetDB = targetDB;
    }

    @Override
    public String eventFormat() {
        return String.format(
                "[%s] " + I18nHelper.copyKey() + "[%s-db%s] " + I18nHelper.targetDatabase() + ":%s",
                connect.getName(), this.data(), dbIndex, this.targetDB
        );
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    public ShellConnect getConnect() {
        return connect;
    }

    public void setConnect(ShellConnect connect) {
        this.connect = connect;
    }
}
