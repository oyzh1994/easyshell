package cn.oyzh.easyshell.mysql.condition;

/**
 * 是NULL条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNullCondition extends MysqlCondition {

    public final static MysqlNullCondition INSTANCE = new MysqlNullCondition();

    public MysqlNullCondition() {
        super("是NULL", "IS NULL", false);
    }
}
