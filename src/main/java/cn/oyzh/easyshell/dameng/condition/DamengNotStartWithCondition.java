package cn.oyzh.easyshell.dameng.condition;

/**
 * 不是开始以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengNotStartWithCondition extends DamengCondition {

    public final static DamengNotStartWithCondition INSTANCE = new DamengNotStartWithCondition();

    public DamengNotStartWithCondition() {
        super("不是开始以", "NOT LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition);
        }
        return super.wrapCondition(condition);
    }
}
