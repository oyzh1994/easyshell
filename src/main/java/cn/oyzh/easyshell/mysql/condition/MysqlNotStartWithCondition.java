package cn.oyzh.easyshell.mysql.condition;

/**
 * 不是开始以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotStartWithCondition extends MysqlCondition {

    public final static MysqlNotStartWithCondition INSTANCE = new MysqlNotStartWithCondition();

    public MysqlNotStartWithCondition() {
        super("不是开始以", "NOT LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition);
        }
        return super.wrapCondition(condition);
    }
}
