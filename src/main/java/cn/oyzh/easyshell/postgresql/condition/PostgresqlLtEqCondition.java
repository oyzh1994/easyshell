package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlLtEqCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlLtEqCondition INSTANCE = new PostgresqlLtEqCondition();

    /**
     * 构造小于等于条件
     */
    public PostgresqlLtEqCondition() {
        super(I18nHelper.ltEq(), "<=");
    }
}
