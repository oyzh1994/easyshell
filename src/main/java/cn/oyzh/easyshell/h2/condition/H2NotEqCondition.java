package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 不等于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2NotEqCondition extends H2Condition {

    public static final H2NotEqCondition INSTANCE = new H2NotEqCondition();

    public H2NotEqCondition() {
        super(I18nHelper.notEq(), "!=");
    }
}
