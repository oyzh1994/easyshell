package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 非空条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbNotNullCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbNotNullCondition INSTANCE = new MariadbNotNullCondition();

    /**
     * 构造非空条件
     */
    public MariadbNotNullCondition() {
        super(I18nHelper.notIsNull(), "IS NOT NULL", false);
    }
}
