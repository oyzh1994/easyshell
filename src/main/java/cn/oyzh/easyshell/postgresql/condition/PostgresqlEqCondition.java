package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlEqCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlEqCondition INSTANCE = new PostgresqlEqCondition();

    /**
     * 构造等于条件
     */
    public PostgresqlEqCondition() {
        super(I18nHelper.eq(), "=");
    }
}
