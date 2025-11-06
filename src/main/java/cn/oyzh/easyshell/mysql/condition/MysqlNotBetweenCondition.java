package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.mysql.DBUtil;

import java.util.Collection;

/**
 * 不介于列表条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MysqlNotBetweenCondition extends MysqlCondition {

    public final static MysqlNotBetweenCondition INSTANCE = new MysqlNotBetweenCondition();

    public MysqlNotBetweenCondition() {
        super("不介于", "NOT BETWEEN");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition instanceof Object[] arr) {
            return this.getValue() + " " + DBUtil.wrapData(arr[0]) + " AND " + DBUtil.wrapData(arr[1]);
        }
        if (condition instanceof Collection coll) {
            return this.getValue() + " " + DBUtil.wrapData(CollectionUtil.get(coll, 0)) + " AND " + DBUtil.wrapData(CollectionUtil.get(coll, 1));
        }
        return super.wrapCondition(condition);
    }
}
