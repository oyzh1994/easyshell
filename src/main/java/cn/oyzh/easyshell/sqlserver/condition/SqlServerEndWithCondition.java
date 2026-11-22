package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值结尾条件
 * <p>
 * SqlServer的LIKE区分大小写，形如'%value'；如需忽略大小写可使用ILIKE。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerEndWithCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerEndWithCondition INSTANCE = new SqlServerEndWithCondition();

    /**
     * 构造以指定值结尾条件
     */
    public SqlServerEndWithCondition() {
        super(I18nHelper.endWith(), "LIKE");
    }

    /**
     * 构造以指定值结尾条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public SqlServerEndWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : this.getValue() + " " + super.wrapValue("%" + condition);
    }
}
