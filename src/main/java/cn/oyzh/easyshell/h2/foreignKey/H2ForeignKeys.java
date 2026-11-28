package cn.oyzh.easyshell.h2.foreignKey;

import cn.oyzh.fx.db.DBObjectList;

import java.util.Collection;

/**
 * H2 外键列表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2ForeignKeys extends DBObjectList<H2ForeignKey> {

    public H2ForeignKeys() {
    }

    public H2ForeignKeys(Collection<H2ForeignKey> foreignKeys) {
        super.addAll(foreignKeys);
    }
}
