package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 为空条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2NullCondition extends H2Condition {

    public static final H2NullCondition INSTANCE = new H2NullCondition();

    public H2NullCondition() {
        super(I18nHelper.isNull(), "IS NULL", false);
    }
}
