package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 空字符串条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2EmptyCondition extends H2Condition {

    public static final H2EmptyCondition INSTANCE = new H2EmptyCondition();

    public H2EmptyCondition() {
        super(I18nHelper.isEmpty(), "= ''", false);
    }
}
