package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 不在列表条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2NotInListCondition extends H2InListCondition {

    public static final H2NotInListCondition INSTANCE = new H2NotInListCondition();

    public H2NotInListCondition() {
        super(I18nHelper.notInList(), "NOT IN");
    }
}
