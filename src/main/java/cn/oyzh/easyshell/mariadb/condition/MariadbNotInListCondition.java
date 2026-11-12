package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不在列表条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbNotInListCondition extends MariadbInListCondition {

    /**
     * 单例实例
     */
    public final static MariadbNotInListCondition INSTANCE = new MariadbNotInListCondition();

    /**
     * 构造不在列表条件
     */
    public MariadbNotInListCondition() {
        super(I18nHelper.notInList(), "NOT IN");
    }
}
