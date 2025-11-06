package cn.oyzh.easyshell.mysql.condition;

/**
 * 结束以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlEndWithCondition extends MysqlCondition {

    public final static MysqlEndWithCondition INSTANCE = new MysqlEndWithCondition();

    public MysqlEndWithCondition() {
        super("结束以", "LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition(condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
