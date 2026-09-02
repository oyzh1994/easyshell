package cn.oyzh.easyshell.dameng.condition;

/**
 * 结束以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengEndWithCondition extends DamengCondition {

    public final static DamengEndWithCondition INSTANCE = new DamengEndWithCondition();

    public DamengEndWithCondition() {
        super("结束以", "LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition(condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
