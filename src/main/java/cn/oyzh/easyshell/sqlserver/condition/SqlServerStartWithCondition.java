package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值开头条件
 * <p>
 * SqlServer的LIKE区分大小写，形如'value%'；如需忽略大小写可使用ILIKE。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerStartWithCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerStartWithCondition INSTANCE = new SqlServerStartWithCondition();

    /**
     * 构造以指定值开头条件
     */
    public SqlServerStartWithCondition() {
        super(I18nHelper.startWith(), "LIKE");
    }

    /**
     * 构造以指定值开头条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public SqlServerStartWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : this.getValue() + " " + super.wrapValue(condition + "%");
    }
}
