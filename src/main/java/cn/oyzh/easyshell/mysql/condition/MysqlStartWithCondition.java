package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.easyshell.mysql.condition.MysqlCondition;

/**
 * 开始以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlStartWithCondition extends MysqlCondition {

    public final static MysqlStartWithCondition INSTANCE = new MysqlStartWithCondition();

    public MysqlStartWithCondition() {
        super("开始以", "LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition);
        }
        return super.wrapCondition(condition);
    }
}
