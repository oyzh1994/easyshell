package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 非空条件
 *
 * @author oyzh
 * @since 2024/6/27
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
}
