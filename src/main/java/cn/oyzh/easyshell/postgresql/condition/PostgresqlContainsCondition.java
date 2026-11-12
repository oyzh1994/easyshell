package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 包含条件
 * <p>
 * PostgreSQL的LIKE区分大小写，形如'%value%'；如需忽略大小写可使用ILIKE，正则匹配可使用~。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlContainsCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlContainsCondition INSTANCE = new PostgresqlContainsCondition();

    /**
     * 构造包含条件
     */
    public PostgresqlContainsCondition() {
        super(I18nHelper.contains(), "LIKE");
    }

    /**
     * 构造包含条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public PostgresqlContainsCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : this.getValue() + " " +  super.wrapCondition("%" + condition + "%");
    }
}
