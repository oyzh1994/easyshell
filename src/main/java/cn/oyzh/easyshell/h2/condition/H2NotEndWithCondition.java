package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 不以指定值结尾条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2NotEndWithCondition extends H2EndWithCondition {

    public static final H2NotEndWithCondition INSTANCE = new H2NotEndWithCondition();

    public H2NotEndWithCondition() {
        super(I18nHelper.notEndWith(), "NOT LIKE");
    }
}
