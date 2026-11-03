package cn.oyzh.easyshell.sqlite.check;

import cn.oyzh.fx.db.DBObjectList;

import java.util.Collection;

/**
 * SQLite 检查约束列表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteChecks extends DBObjectList<SQLiteCheck> {

    public SQLiteChecks() {
    }

    public SQLiteChecks(Collection<SQLiteCheck> checks) {
        super.addAll(checks);
    }
}
