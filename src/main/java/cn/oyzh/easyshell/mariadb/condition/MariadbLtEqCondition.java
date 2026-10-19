package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于等于条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbLtEqCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbLtEqCondition INSTANCE = new MariadbLtEqCondition();

    /**
     * 构造小于等于条件
     */
    public MariadbLtEqCondition() {
        super(I18nHelper.ltEq(), "<=");
    }
}
