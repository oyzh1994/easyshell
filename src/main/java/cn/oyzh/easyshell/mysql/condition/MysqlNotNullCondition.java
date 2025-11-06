package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.easyshell.mysql.condition.MysqlCondition;

/**
 * 不是NULL条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotNullCondition extends MysqlCondition {

    public final static MysqlNotNullCondition INSTANCE = new MysqlNotNullCondition();

    public MysqlNotNullCondition() {
        super("不是NULL", "IS NOT NULL", false);
    }
}
