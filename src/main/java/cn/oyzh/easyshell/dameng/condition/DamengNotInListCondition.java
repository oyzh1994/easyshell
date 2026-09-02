package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * 不在列表条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class DamengNotInListCondition extends DamengCondition {

    public final static DamengNotInListCondition INSTANCE = new DamengNotInListCondition();

    public DamengNotInListCondition() {
        super("不在列表", "NOT IN");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return this.getValue() + " (" + DBUtil.wrapData(condition, DBDialect.DAMENG) + ")";
        }
        return super.wrapCondition(condition);
    }
}
