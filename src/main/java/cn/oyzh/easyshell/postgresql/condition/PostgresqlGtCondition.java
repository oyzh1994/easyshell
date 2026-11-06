package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.postgresql.condition.PostgresqlCondition;

/**
 * 大于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlGtCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlGtCondition INSTANCE = new PostgresqlGtCondition();

    /**
     * 构造大于条件
     */
    public PostgresqlGtCondition() {
        super(I18nHelper.gt(), ">");
    }
}
