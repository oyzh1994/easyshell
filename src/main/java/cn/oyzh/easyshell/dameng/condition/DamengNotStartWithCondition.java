package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值开头条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengNotStartWithCondition extends DamengStartWithCondition {

    /**
     * 单例实例
     */
    public final static DamengNotStartWithCondition INSTANCE = new DamengNotStartWithCondition();

    /**
     * 构造不以指定值开头条件
     */
    public DamengNotStartWithCondition() {
        super(I18nHelper.notStartWith(), "NOT LIKE");
    }
}
