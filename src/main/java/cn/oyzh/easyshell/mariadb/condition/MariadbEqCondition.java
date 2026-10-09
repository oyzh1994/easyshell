package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 等于条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbEqCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbEqCondition INSTANCE = new MariadbEqCondition();

    /**
     * 构造等于条件
     */
    public MariadbEqCondition() {
        super(I18nHelper.eq(), "=");
    }
}
