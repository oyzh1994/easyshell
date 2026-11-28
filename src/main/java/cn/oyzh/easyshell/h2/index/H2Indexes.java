package cn.oyzh.easyshell.h2.index;

import cn.oyzh.fx.db.DBObjectList;

import java.util.Collection;

/**
 * H2 索引列表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2Indexes extends DBObjectList<H2Index> {

    public H2Indexes() {
    }

    public H2Indexes(Collection<H2Index> indexes) {
        super.addAll(indexes);
    }
}
