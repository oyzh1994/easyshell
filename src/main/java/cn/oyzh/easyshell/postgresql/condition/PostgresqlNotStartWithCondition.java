package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值开头条件
 * <p>
 * PostgreSQL的NOT LIKE区分大小写，形如'value%'；如需忽略大小写可使用NOT ILIKE。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlNotStartWithCondition extends PostgresqlStartWithCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlNotStartWithCondition INSTANCE = new PostgresqlNotStartWithCondition();

    /**
     * 构造不以指定值开头条件
     */
    public PostgresqlNotStartWithCondition() {
        super(I18nHelper.notStartWith(), "NOT LIKE");
    }
}
