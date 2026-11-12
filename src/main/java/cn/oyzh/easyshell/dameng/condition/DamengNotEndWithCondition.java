package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值结尾条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengNotEndWithCondition extends DamengEndWithCondition {

    /**
     * 单例实例
     */
    public final static DamengNotEndWithCondition INSTANCE = new DamengNotEndWithCondition();

    /**
     * 构造不以指定值结尾条件
     */
    public DamengNotEndWithCondition() {
        super(I18nHelper.notEndWith(), "NOT LIKE");
    }

}
