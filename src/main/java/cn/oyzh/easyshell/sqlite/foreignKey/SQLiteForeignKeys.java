package cn.oyzh.easyshell.sqlite.foreignKey;

import cn.oyzh.fx.db.DBObjectList;

import java.util.Collection;

/**
 * SQLite 外键列表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteForeignKeys extends DBObjectList<SQLiteForeignKey> {

    public SQLiteForeignKeys() {
    }

    public SQLiteForeignKeys(Collection<SQLiteForeignKey> foreignKeys) {
        super.addAll(foreignKeys);
    }
}
