package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 不介于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2NotBetweenCondition extends H2BetweenCondition {

    public static final H2NotBetweenCondition INSTANCE = new H2NotBetweenCondition();

    public H2NotBetweenCondition() {
        super(I18nHelper.notBetween(), "NOT BETWEEN");
    }
}
