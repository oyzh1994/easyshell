package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不是开始以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotStartWithCondition extends MysqlStartWithCondition {

    public final static MysqlNotStartWithCondition INSTANCE = new MysqlNotStartWithCondition();

    public MysqlNotStartWithCondition() {
        super(I18nHelper.notStartWith(), "NOT LIKE");
    }

    // @Override
    // public String wrapCondition(Object condition) {
    //     if (condition != null) {
    //         return super.wrapCondition("%" + condition);
    //     }
    //     return super.wrapCondition(condition);
    // }
}
