package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 是NULL条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengNullCondition extends DamengCondition {

    public final static DamengNullCondition INSTANCE = new DamengNullCondition();

    public DamengNullCondition() {
        super(I18nHelper.isNull(), "IS NULL", false);
    }
}
