package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 非空字符串条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2NotEmptyCondition extends H2Condition {

    public static final H2NotEmptyCondition INSTANCE = new H2NotEmptyCondition();

    public H2NotEmptyCondition() {
        super(I18nHelper.notIsEmpty(), "<> ''", false);
    }
}
