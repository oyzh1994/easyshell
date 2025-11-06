package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.easyshell.util.mysql.DBUtil;

/**
 * 在列表条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MysqlInListCondition extends MysqlCondition {

    public final static MysqlInListCondition INSTANCE = new MysqlInListCondition();

    public MysqlInListCondition() {
        super("在列表", "IN");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return this.getValue() + " (" + DBUtil.wrapData(condition) + ")";
        }
        return super.wrapCondition(condition);
    }
}
