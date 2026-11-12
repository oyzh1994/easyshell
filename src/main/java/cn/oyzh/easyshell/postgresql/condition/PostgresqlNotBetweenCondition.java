package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不介于区间条件
 * <p>
 * PostgreSQL使用NOT BETWEEN ... AND ...，区间值按PostgreSQL字面量规则包装。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlNotBetweenCondition extends PostgresqlBetweenCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlNotBetweenCondition INSTANCE = new PostgresqlNotBetweenCondition();

    /**
     * 构造不介于区间条件
     */
    public PostgresqlNotBetweenCondition() {
        super(I18nHelper.notBetween(), "NOT BETWEEN");
    }
}
