package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.dameng.condition.DamengCondition;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengEmptyCondition extends DamengCondition {

    public final static DamengEmptyCondition INSTANCE = new DamengEmptyCondition();

    public DamengEmptyCondition() {
        super(I18nHelper.isEmpty(), "=''", false);
    }

}
