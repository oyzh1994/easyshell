package cn.oyzh.easyshell.mysql.condition;

/**
 * 等于条件
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlEqCondition extends MysqlCondition {

    public final static MysqlEqCondition INSTANCE = new MysqlEqCondition();

    public MysqlEqCondition() {
        super("等于", "=");
    }
}
