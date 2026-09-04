package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不是NULL条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengNotNullCondition extends DamengCondition {

    public final static DamengNotNullCondition INSTANCE = new DamengNotNullCondition();

    public DamengNotNullCondition() {
        super(I18nHelper.notIsNull(), "IS NOT NULL", false);
    }
}
