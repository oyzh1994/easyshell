package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengEqCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengEqCondition INSTANCE = new DamengEqCondition();

    /**
     * 构造等于条件
     */
    public DamengEqCondition() {
        super(I18nHelper.eq(), "=");
    }
}
