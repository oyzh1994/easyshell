package cn.oyzh.easyshell.dameng.condition;

/**
 * 是NULL条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengNullCondition extends DamengCondition {

    public final static DamengNullCondition INSTANCE = new DamengNullCondition();

    public DamengNullCondition() {
        super("是NULL", "IS NULL", false);
    }
}
