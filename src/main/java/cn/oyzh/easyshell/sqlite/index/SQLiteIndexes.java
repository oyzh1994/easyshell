package cn.oyzh.easyshell.sqlite.index;

import cn.oyzh.fx.db.DBObjectList;

import java.util.Collection;

/**
 * SQLite 索引列表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteIndexes extends DBObjectList<SQLiteIndex> {

    public SQLiteIndexes() {
    }

    public SQLiteIndexes(Collection<SQLiteIndex> indexes) {
        super.addAll(indexes);
    }
}
