package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不是结束以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengNotEndWithCondition extends DamengCondition {

    public final static DamengNotEndWithCondition INSTANCE = new DamengNotEndWithCondition();

    public DamengNotEndWithCondition() {
        super(I18nHelper.notEndWith(), "NOT LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition(condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
