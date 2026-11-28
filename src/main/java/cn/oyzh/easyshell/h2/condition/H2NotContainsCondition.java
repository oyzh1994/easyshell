package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 不包含条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2NotContainsCondition extends H2ContainsCondition {

    public static final H2NotContainsCondition INSTANCE = new H2NotContainsCondition();

    public H2NotContainsCondition() {
        super(I18nHelper.notContains(), "NOT LIKE");
    }
}
