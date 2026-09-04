package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于等于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengLtEqCondition extends DamengCondition {

    public final static DamengLtEqCondition INSTANCE = new DamengLtEqCondition();

    public DamengLtEqCondition() {
        super(I18nHelper.ltEq(), "<=");
    }
}
