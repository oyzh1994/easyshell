package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值结尾条件
 * <p>
 * PostgreSQL的NOT LIKE区分大小写，形如'%value'；如需忽略大小写可使用NOT ILIKE。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlNotEndWithCondition extends PostgresqlEndWithCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlNotEndWithCondition INSTANCE = new PostgresqlNotEndWithCondition();

    /**
     * 构造不以指定值结尾条件
     */
    public PostgresqlNotEndWithCondition() {
        super(I18nHelper.notEndWith(), "NOT LIKE");
    }
}
