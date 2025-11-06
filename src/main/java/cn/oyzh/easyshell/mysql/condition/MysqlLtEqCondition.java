package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.easyshell.mysql.condition.MysqlCondition;

/**
 * 小于等于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlLtEqCondition extends MysqlCondition {

    public final static MysqlLtEqCondition INSTANCE = new MysqlLtEqCondition();

    public MysqlLtEqCondition() {
        super("小于等于", "<=");
    }
}
