package cn.oyzh.easyshell.dameng.condition;

/**
 * 开始以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengStartWithCondition extends DamengCondition {

    public final static DamengStartWithCondition INSTANCE = new DamengStartWithCondition();

    public DamengStartWithCondition() {
        super("开始以", "LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition);
        }
        return super.wrapCondition(condition);
    }
}
