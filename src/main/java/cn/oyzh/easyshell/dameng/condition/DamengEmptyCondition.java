package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.dameng.condition.DamengCondition;

/**
 * 为空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengEmptyCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengEmptyCondition INSTANCE = new DamengEmptyCondition();

    /**
     * 构造为空条件
     */
    public DamengEmptyCondition() {
        super(I18nHelper.isEmpty(), "=''", false);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : condition.toString();
    }
}
