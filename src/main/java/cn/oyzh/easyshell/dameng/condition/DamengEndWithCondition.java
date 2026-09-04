package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 结束以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengEndWithCondition extends DamengCondition {

    public final static DamengEndWithCondition INSTANCE = new DamengEndWithCondition();

    public DamengEndWithCondition() {
        super(I18nHelper.endWith(), "LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition(condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
