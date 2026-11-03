package cn.oyzh.easyshell.sqlite.trigger;

import cn.oyzh.fx.db.DBObjectList;

import java.util.Collection;

/**
 * SQLite 触发器列表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteTriggers extends DBObjectList<SQLiteTrigger> {

    public SQLiteTriggers() {
    }

    public SQLiteTriggers(Collection<SQLiteTrigger> triggers) {
        super.addAll(triggers);
    }
}
