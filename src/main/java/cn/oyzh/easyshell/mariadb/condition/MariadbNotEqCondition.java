package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不等于条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbNotEqCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbNotEqCondition INSTANCE = new MariadbNotEqCondition();

    /**
     * 构造不等于条件
     */
    public MariadbNotEqCondition() {
        super(I18nHelper.notEq(), "!=");
    }
}
