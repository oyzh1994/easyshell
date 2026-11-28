package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 大于等于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2GtEqCondition extends H2Condition {

    public static final H2GtEqCondition INSTANCE = new H2GtEqCondition();

    public H2GtEqCondition() {
        super(I18nHelper.gtEq(), ">=");
    }
}
