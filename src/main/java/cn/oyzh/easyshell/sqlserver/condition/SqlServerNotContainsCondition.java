package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不包含条件
 * <p>
 * SqlServer的NOT LIKE区分大小写，形如'%value%'；如需忽略大小写可使用NOT ILIKE，正则匹配可使用!~。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerNotContainsCondition extends SqlServerContainsCondition {

    /**
     * 单例实例
     */
    public final static SqlServerNotContainsCondition INSTANCE = new SqlServerNotContainsCondition();

    /**
     * 构造不包含条件
     */
    public SqlServerNotContainsCondition() {
        super(I18nHelper.notContains(), "NOT LIKE");
    }
}
