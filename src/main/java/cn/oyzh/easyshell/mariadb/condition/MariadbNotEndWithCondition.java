package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值结尾条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbNotEndWithCondition extends MariadbEndWithCondition {

    /**
     * 单例实例
     */
    public final static MariadbNotEndWithCondition INSTANCE = new MariadbNotEndWithCondition();

    /**
     * 构造不以指定值结尾条件
     */
    public MariadbNotEndWithCondition() {
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
