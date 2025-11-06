package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.easyshell.mysql.condition.MysqlCondition;

/**
 * 大于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlGtCondition extends MysqlCondition {

    public final static MysqlGtCondition INSTANCE = new MysqlGtCondition();

    public MysqlGtCondition() {
        super("大于", ">");
    }
}
