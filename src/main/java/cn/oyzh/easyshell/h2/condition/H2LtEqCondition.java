package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 小于等于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2LtEqCondition extends H2Condition {

    public static final H2LtEqCondition INSTANCE = new H2LtEqCondition();

    public H2LtEqCondition() {
        super(I18nHelper.ltEq(), "<=");
    }
}
