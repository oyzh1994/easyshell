package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不包含条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotContainsCondition extends MysqlContainsCondition {

    public final static MysqlNotContainsCondition INSTANCE = new MysqlNotContainsCondition();

    public MysqlNotContainsCondition() {
        super(I18nHelper.notContains(), "NOT LIKE");
    }

    // @Override
    // public String wrapCondition(Object condition) {
    //     if (condition != null) {
    //         return super.wrapCondition("%" + condition + "%");
    //     }
    //     return super.wrapCondition(condition);
    // }
}
