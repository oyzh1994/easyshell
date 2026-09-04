package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 开始以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengStartWithCondition extends DamengCondition {

    public final static DamengStartWithCondition INSTANCE = new DamengStartWithCondition();

    public DamengStartWithCondition() {
        super(I18nHelper.startWith(), "LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition);
        }
        return super.wrapCondition(condition);
    }
}
