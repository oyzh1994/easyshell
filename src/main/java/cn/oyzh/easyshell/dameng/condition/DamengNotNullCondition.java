package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 非空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengNotNullCondition extends DamengCondition {

    /**
     * 单例实例
     */
    public final static DamengNotNullCondition INSTANCE = new DamengNotNullCondition();

    /**
     * 构造非空条件
     */
    public DamengNotNullCondition() {
        super(I18nHelper.notIsNull(), "IS NOT NULL", false);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : condition.toString();
    }
}
