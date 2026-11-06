package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不等于条件
 * <p>
 * PostgreSQL使用&lt;&gt;表示不等于。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlNotEqCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlNotEqCondition INSTANCE = new PostgresqlNotEqCondition();

    /**
     * 构造不等于条件
     */
    public PostgresqlNotEqCondition() {
        super(I18nHelper.notEq(), "<>");
    }
}
