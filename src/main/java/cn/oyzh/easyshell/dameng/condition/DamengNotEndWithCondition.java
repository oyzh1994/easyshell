package cn.oyzh.easyshell.dameng.condition;

/**
 * 不是结束以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class DamengNotEndWithCondition extends DamengCondition {

    public final static DamengNotEndWithCondition INSTANCE = new DamengNotEndWithCondition();

    public DamengNotEndWithCondition() {
        super("不是结束以", "NOT LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition(condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
