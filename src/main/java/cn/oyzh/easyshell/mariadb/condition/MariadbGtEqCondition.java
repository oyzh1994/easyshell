package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 大于等于条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbGtEqCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbGtEqCondition INSTANCE = new MariadbGtEqCondition();

    /**
     * 构造大于等于条件
     */
    public MariadbGtEqCondition() {
        super(I18nHelper.gtEq(), ">=");
    }
}
