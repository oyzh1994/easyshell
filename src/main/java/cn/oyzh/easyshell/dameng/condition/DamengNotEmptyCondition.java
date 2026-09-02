package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.easyshell.dameng.condition.DamengCondition;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengNotEmptyCondition extends DamengCondition {

    public final static DamengNotEmptyCondition INSTANCE = new DamengNotEmptyCondition();

    public DamengNotEmptyCondition() {
        super("不是空的", "!=''", false);
    }

}
