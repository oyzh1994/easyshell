package cn.oyzh.easyshell.dameng.condition;

/**
 * 等于条件
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengEqCondition extends DamengCondition {

    public final static DamengEqCondition INSTANCE = new DamengEqCondition();

    public DamengEqCondition() {
        super("等于", "=");
    }
}
