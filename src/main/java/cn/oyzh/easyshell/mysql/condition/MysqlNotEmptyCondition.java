package cn.oyzh.easyshell.mysql.condition;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotEmptyCondition extends MysqlCondition {

    public final static MysqlNotEmptyCondition INSTANCE = new MysqlNotEmptyCondition();

    public MysqlNotEmptyCondition() {
        super("不是空的", "!=''", false);
    }

}
