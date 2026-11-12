package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不包含条件
 * <p>
 * PostgreSQL的NOT LIKE区分大小写，形如'%value%'；如需忽略大小写可使用NOT ILIKE，正则匹配可使用!~。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlNotContainsCondition extends PostgresqlContainsCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlNotContainsCondition INSTANCE = new PostgresqlNotContainsCondition();

    /**
     * 构造不包含条件
     */
    public PostgresqlNotContainsCondition() {
        super(I18nHelper.notContains(), "NOT LIKE");
    }
}
