package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值结尾条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlNotEndWithCondition extends MysqlEndWithCondition {

    /**
     * 单例实例
     */
    public final static MysqlNotEndWithCondition INSTANCE = new MysqlNotEndWithCondition();

    /**
     * 构造不以指定值结尾条件
     */
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
