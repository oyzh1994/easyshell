package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 包含条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2ContainsCondition extends H2Condition {

    public static final H2ContainsCondition INSTANCE = new H2ContainsCondition();

    public H2ContainsCondition() {
        super(I18nHelper.contains(), "LIKE");
    }

    public H2ContainsCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null :  this.getValue() + " " + super.wrapValue("%" + condition + "%");
    }
}
