package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 大于条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbGtCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbGtCondition INSTANCE = new MariadbGtCondition();

    /**
     * 构造大于条件
     */
    public MariadbGtCondition() {
        super(I18nHelper.gt(), ">");
    }
}
