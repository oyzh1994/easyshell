package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.dameng.condition.DamengCondition;

/**
 * 不包含条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengNotContainsCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengNotContainsCondition INSTANCE = new DamengNotContainsCondition();

    /**
     * 构造不包含条件
     */
    public DamengNotContainsCondition() {
        super(I18nHelper.notContains(), "NOT LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
