package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;

/**
 * 不在列表条件
 *
 * @author oyzh
 * @since 2024/6/28
 */
public class MysqlNotInListCondition extends MysqlCondition {

    public final static MysqlNotInListCondition INSTANCE = new MysqlNotInListCondition();

    public MysqlNotInListCondition() {
        super("不在列表", "NOT IN");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return this.getValue() + " (" + ShellMysqlUtil.wrapData(condition) + ")";
        }
        return super.wrapCondition(condition);
    }
}
