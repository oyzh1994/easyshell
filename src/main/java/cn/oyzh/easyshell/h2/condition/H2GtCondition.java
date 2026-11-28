package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 大于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2GtCondition extends H2Condition {

    public static final H2GtCondition INSTANCE = new H2GtCondition();

    public H2GtCondition() {
        super(I18nHelper.gt(), ">");
    }
}
