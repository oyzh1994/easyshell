package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 介于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2BetweenCondition extends H2Condition {

    public static final H2BetweenCondition INSTANCE = new H2BetweenCondition();

    public H2BetweenCondition() {
        super(I18nHelper.between(), "BETWEEN");
    }

    public H2BetweenCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return this.getValue() + " " + super.wrapBetweenCondition(condition);
    }
}
