package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.dameng.condition.DamengCondition;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.Collection;

/**
 * 不介于列表条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class DamengNotBetweenCondition extends DamengCondition {

    public final static DamengNotBetweenCondition INSTANCE = new DamengNotBetweenCondition();

    public DamengNotBetweenCondition() {
        super("不介于", "NOT BETWEEN");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition instanceof Object[] arr) {
            return this.getValue() + " " + DBUtil.wrapData(arr[0], DBDialect.DAMENG) + " AND " + DBUtil.wrapData(arr[1], DBDialect.DAMENG);
        }
        if (condition instanceof Collection coll) {
            return this.getValue() + " " + DBUtil.wrapData(CollectionUtil.get(coll, 0), DBDialect.DAMENG) + " AND " + DBUtil.wrapData(CollectionUtil.get(coll, 1), DBDialect.DAMENG);
        }
        return super.wrapCondition(condition);
    }
}
