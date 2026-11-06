package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值结尾条件
 * <p>
 * PostgreSQL的LIKE区分大小写，形如'%value'；如需忽略大小写可使用ILIKE。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlEndWithCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlEndWithCondition INSTANCE = new PostgresqlEndWithCondition();

    /**
     * 构造以指定值结尾条件
     */
    public PostgresqlEndWithCondition() {
        super(I18nHelper.endWith(), "LIKE");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition);
        }
        return super.wrapCondition(condition);
    }
}
