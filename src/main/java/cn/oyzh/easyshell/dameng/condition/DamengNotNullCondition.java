package cn.oyzh.easyshell.dameng.condition;

/**
 * 不是NULL条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengNotNullCondition extends DamengCondition {

    public final static DamengNotNullCondition INSTANCE = new DamengNotNullCondition();

    public DamengNotNullCondition() {
        super("不是NULL", "IS NOT NULL", false);
    }
}
