package cn.oyzh.easyshell.mysql.condition;

/**
 * 不包含条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotContainsCondition extends MysqlCondition {

    public final static MysqlNotContainsCondition INSTANCE = new MysqlNotContainsCondition();

    public MysqlNotContainsCondition() {
        super("不包含", "NOT LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
