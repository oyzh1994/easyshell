package cn.oyzh.easyshell.h2.trigger;

import cn.oyzh.fx.db.DBObjectList;

import java.util.Collection;

/**
 * H2 触发器列表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2Triggers extends DBObjectList<H2Trigger> {

    public H2Triggers() {
    }

    public H2Triggers(Collection<H2Trigger> triggers) {
        super.addAll(triggers);
    }
}
