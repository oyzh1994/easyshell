package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.postgresql.condition.PostgresqlCondition;

/**
 * 大于等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlGtEqCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlGtEqCondition INSTANCE = new PostgresqlGtEqCondition();

    /**
     * 构造大于等于条件
     */
    public PostgresqlGtEqCondition() {
        super(I18nHelper.gtEq(), ">=");
    }
}
