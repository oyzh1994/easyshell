package cn.oyzh.easyshell.mysql.condition;

/**
 * 不等于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotEqCondition extends MysqlCondition {

    public final static MysqlNotEqCondition INSTANCE = new MysqlNotEqCondition();

    public MysqlNotEqCondition() {
        super("不等于", "!=");
    }
}
