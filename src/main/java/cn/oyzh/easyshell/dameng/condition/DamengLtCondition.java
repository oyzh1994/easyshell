package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengLtCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengLtCondition INSTANCE = new DamengLtCondition();

    /**
     * 构造小于条件
     */
    public DamengLtCondition() {
        super(I18nHelper.lt(), "<");
    }
}
