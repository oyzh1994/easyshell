package cn.oyzh.easyshell.mysql.condition;

/**
 * 小于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlLtCondition extends MysqlCondition {

    public final static MysqlLtCondition INSTANCE = new MysqlLtCondition();

    public MysqlLtCondition() {
        super("小于", "<");
    }
}
