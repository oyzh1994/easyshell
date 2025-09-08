package cn.oyzh.easyshell.event.redis;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * @author oyzh
 * @since 2023/12/12
 */
public class ShellRedisKeysCopiedEvent extends Event<List<String>> implements EventFormatter {

    private int sourceDB;

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
                connect.getName(), this.data(), sourceDB, this.targetDB
        );
    }

    public int getSourceDB() {
        return sourceDB;
    }

    public void setSourceDB(int sourceDB) {
        this.sourceDB = sourceDB;
    }

    public ShellConnect getConnect() {
        return connect;
    }

    public void setConnect(ShellConnect connect) {
        this.connect = connect;
    }
}
