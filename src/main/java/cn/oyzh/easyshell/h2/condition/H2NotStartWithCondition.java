package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 不以指定值开头条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2NotStartWithCondition extends H2StartWithCondition {

    public static final H2NotStartWithCondition INSTANCE = new H2NotStartWithCondition();

    public H2NotStartWithCondition() {
        super(I18nHelper.notStartWith(), "NOT LIKE");
    }
}
