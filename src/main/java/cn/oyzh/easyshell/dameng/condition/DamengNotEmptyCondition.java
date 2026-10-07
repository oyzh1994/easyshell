package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.dameng.condition.DamengCondition;

/**
 * 不为空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengNotEmptyCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengNotEmptyCondition INSTANCE = new DamengNotEmptyCondition();

    /**
     * 构造不为空条件
     */
    public DamengNotEmptyCondition() {
        super(I18nHelper.notIsEmpty(), "!=''", false);
    }

}
