package cn.oyzh.easyshell.dameng.condition;

/**
 * 不等于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengNotEqCondition extends DamengCondition {

    public final static DamengNotEqCondition INSTANCE = new DamengNotEqCondition();

    public DamengNotEqCondition() {
        super("不等于", "!=");
    }
}
