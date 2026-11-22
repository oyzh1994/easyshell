package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值开头条件
 * <p>
 * SqlServer的NOT LIKE区分大小写，形如'value%'；如需忽略大小写可使用NOT ILIKE。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerNotStartWithCondition extends SqlServerStartWithCondition {

    /**
     * 单例实例
     */
    public final static SqlServerNotStartWithCondition INSTANCE = new SqlServerNotStartWithCondition();

    /**
     * 构造不以指定值开头条件
     */
    public SqlServerNotStartWithCondition() {
        super(I18nHelper.notStartWith(), "NOT LIKE");
    }
}
