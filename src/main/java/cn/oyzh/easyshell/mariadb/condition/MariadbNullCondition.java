package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 为空条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbNullCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbNullCondition INSTANCE = new MariadbNullCondition();

    /**
     * 构造为空条件
     */
    public MariadbNullCondition() {
        super(I18nHelper.isNull(), "IS NULL", false);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : condition.toString();
    }
}
