package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.dameng.condition.DamengCondition;

/**
 * 大于等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengGtEqCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengGtEqCondition INSTANCE = new DamengGtEqCondition();

    /**
     * 构造大于等于条件
     */
    public DamengGtEqCondition() {
        super(I18nHelper.gtEq(), ">=");
    }
}
