package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不是结束以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotEndWithCondition extends MysqlEndWithCondition {

    public final static MysqlNotEndWithCondition INSTANCE = new MysqlNotEndWithCondition();

    public MysqlNotEndWithCondition() {
        super(I18nHelper.notEndWith(), "NOT LIKE");
    }

    // @Override
    // public String wrapCondition(Object condition) {
    //     if (condition != null) {
    //         return super.wrapCondition(condition + "%");
    //     }
    //     return super.wrapCondition(condition);
    // }
}
