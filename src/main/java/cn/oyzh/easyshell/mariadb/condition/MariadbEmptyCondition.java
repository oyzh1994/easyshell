package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 为空条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbEmptyCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbEmptyCondition INSTANCE = new MariadbEmptyCondition();

    /**
     * 构造为空条件
     */
    public MariadbEmptyCondition() {
        super(I18nHelper.isEmpty(), "=''", false);
    }

}
