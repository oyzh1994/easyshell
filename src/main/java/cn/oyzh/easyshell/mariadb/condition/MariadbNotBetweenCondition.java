package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不介于区间条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbNotBetweenCondition extends MariadbBetweenCondition {

    /**
     * 单例实例
     */
    public final static MariadbNotBetweenCondition INSTANCE = new MariadbNotBetweenCondition();

    /**
     * 构造不介于区间条件
     */
    public MariadbNotBetweenCondition() {
        super(I18nHelper.notBetween(), "NOT BETWEEN");
    }
}
