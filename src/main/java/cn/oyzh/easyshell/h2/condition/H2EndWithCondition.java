package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * H2 以指定值结尾条件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2EndWithCondition extends H2Condition {

    public static final H2EndWithCondition INSTANCE = new H2EndWithCondition();

    public H2EndWithCondition() {
        super(I18nHelper.endWith(), "LIKE");
    }

    /**
     * 构造以指定值结尾条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public H2EndWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null :  this.getValue() + " " + super.wrapValue("%" + condition);
    }
}
