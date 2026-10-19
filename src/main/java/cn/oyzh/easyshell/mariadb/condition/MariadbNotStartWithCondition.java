package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值开头条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbNotStartWithCondition extends MariadbStartWithCondition {

    /**
     * 单例实例
     */
    public final static MariadbNotStartWithCondition INSTANCE = new MariadbNotStartWithCondition();

    /**
     * 构造不以指定值开头条件
     */
    public MariadbNotStartWithCondition() {
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
