package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 非空条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2NotNullCondition extends H2Condition {

    public static final H2NotNullCondition INSTANCE = new H2NotNullCondition();

    public H2NotNullCondition() {
        super(I18nHelper.notIsNull(), "IS NOT NULL", false);
    }
}
