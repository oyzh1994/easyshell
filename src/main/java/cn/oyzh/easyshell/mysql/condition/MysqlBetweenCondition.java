package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;

import java.util.Collection;

/**
 * 介于条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MysqlBetweenCondition extends MysqlCondition {

    public final static MysqlBetweenCondition INSTANCE = new MysqlBetweenCondition();

    public MysqlBetweenCondition() {
        super("介于", "BETWEEN");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition instanceof Object[] arr) {
            return this.getValue() + " " + ShellMysqlUtil.wrapData(arr[0]) + " AND " + ShellMysqlUtil.wrapData(arr[1]);
        }
        if (condition instanceof Collection coll) {
            return this.getValue() + " " + ShellMysqlUtil.wrapData(CollectionUtil.get(coll, 0)) + " AND " + ShellMysqlUtil.wrapData(CollectionUtil.get(coll, 1));
        }
        return super.wrapCondition(condition);
    }
}
