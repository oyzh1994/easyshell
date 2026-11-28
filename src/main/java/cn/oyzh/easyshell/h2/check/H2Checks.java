package cn.oyzh.easyshell.h2.check;

import cn.oyzh.fx.db.DBObjectList;

import java.util.Collection;

/**
 * H2 检查约束列表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2Checks extends DBObjectList<H2Check> {

    public H2Checks() {
    }

    public H2Checks(Collection<H2Check> checks) {
        super.addAll(checks);
    }
}
