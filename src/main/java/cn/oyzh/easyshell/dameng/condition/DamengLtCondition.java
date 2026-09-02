package cn.oyzh.easyshell.dameng.condition;

/**
 * 小于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengLtCondition extends DamengCondition {

    public final static DamengLtCondition INSTANCE = new DamengLtCondition();

    public DamengLtCondition() {
        super("小于", "<");
    }
}
