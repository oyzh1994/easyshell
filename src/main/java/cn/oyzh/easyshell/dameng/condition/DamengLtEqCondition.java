package cn.oyzh.easyshell.dameng.condition;

/**
 * 小于等于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengLtEqCondition extends DamengCondition {

    public final static DamengLtEqCondition INSTANCE = new DamengLtEqCondition();

    public DamengLtEqCondition() {
        super("小于等于", "<=");
    }
}
