package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 以指定值开头条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2StartWithCondition extends H2Condition {

    public static final H2StartWithCondition INSTANCE = new H2StartWithCondition();

    public H2StartWithCondition() {
        super(I18nHelper.startWith(), "LIKE");
    }

    public H2StartWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : this.getValue() + " " +  super.wrapValue(condition + "%");
    }
}
