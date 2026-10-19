package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbContainsCondition extends MariadbCondition {

    /**
     * 单例实例
     */
    public final static MariadbContainsCondition INSTANCE = new MariadbContainsCondition();

    /**
     * 构造包含条件
     */
    public MariadbContainsCondition() {
        super(I18nHelper.contains(), "LIKE");
    }

    /**
     * 构造包含条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public MariadbContainsCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
