package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.easyshell.mysql.condition.MysqlCondition;

/**
 * 不是结束以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotEndWithCondition extends MysqlCondition {

    public final static MysqlNotEndWithCondition INSTANCE = new MysqlNotEndWithCondition();

    public MysqlNotEndWithCondition() {
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
