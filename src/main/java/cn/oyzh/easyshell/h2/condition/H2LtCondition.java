package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 小于条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2LtCondition extends H2Condition {

    public static final H2LtCondition INSTANCE = new H2LtCondition();

    public H2LtCondition() {
        super(I18nHelper.lt(), "<");
    }
}
