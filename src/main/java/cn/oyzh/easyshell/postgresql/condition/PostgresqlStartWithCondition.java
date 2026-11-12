package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值开头条件
 * <p>
 * PostgreSQL的LIKE区分大小写，形如'value%'；如需忽略大小写可使用ILIKE。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlStartWithCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlStartWithCondition INSTANCE = new PostgresqlStartWithCondition();

    /**
     * 构造以指定值开头条件
     */
    public PostgresqlStartWithCondition() {
        super(I18nHelper.startWith(), "LIKE");
    }

    /**
     * 构造以指定值开头条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public PostgresqlStartWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null :  this.getValue() + " " + super.wrapCondition(condition + "%");
    }
}
