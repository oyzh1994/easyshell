package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.dameng.condition.DamengCondition;

/**
 * 大于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengGtCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengGtCondition INSTANCE = new DamengGtCondition();

    /**
     * 构造大于条件
     */
    public DamengGtCondition() {
        super(I18nHelper.gt(), ">");
    }
}
