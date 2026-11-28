package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 在列表条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2InListCondition extends H2Condition {

    public static final H2InListCondition INSTANCE = new H2InListCondition();

    public H2InListCondition() {
        super(I18nHelper.inList(), "IN");
    }

    public H2InListCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return this.getValue() + " " + super.wrapListCondition(condition);
    }
}
