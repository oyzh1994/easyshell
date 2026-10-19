package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbLtCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbLtCondition INSTANCE = new MariadbLtCondition();

    /**
     * 构造小于条件
     */
    public MariadbLtCondition() {
        super(I18nHelper.lt(), "<");
    }
}
