package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不为空条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbNotEmptyCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbNotEmptyCondition INSTANCE = new MariadbNotEmptyCondition();

    /**
     * 构造不为空条件
     */
    public MariadbNotEmptyCondition() {
        super(I18nHelper.notIsEmpty(), "!=''", false);
    }

}
