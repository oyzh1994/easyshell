package cn.oyzh.easyshell.mysql.condition;

/**
 * 大于等于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlGtEqCondition extends MysqlCondition {

    public final static MysqlGtEqCondition INSTANCE = new MysqlGtEqCondition();

    public MysqlGtEqCondition() {
        super("大于等于", ">=");
    }
}
