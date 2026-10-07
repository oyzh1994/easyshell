package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于等于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengLtEqCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengLtEqCondition INSTANCE = new DamengLtEqCondition();

    /**
     * 构造小于等于条件
     */
    public DamengLtEqCondition() {
        super(I18nHelper.ltEq(), "<=");
    }
}
