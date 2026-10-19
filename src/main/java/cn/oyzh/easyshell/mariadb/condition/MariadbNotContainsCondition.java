package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不包含条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbNotContainsCondition extends MariadbContainsCondition {

    /**
     * 单例实例
     */
    public final static MariadbNotContainsCondition INSTANCE = new MariadbNotContainsCondition();

    /**
     * 构造不包含条件
     */
    public MariadbNotContainsCondition() {
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
