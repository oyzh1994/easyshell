package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值结尾条件
 * <p>
 * SqlServer的NOT LIKE区分大小写，形如'%value'；如需忽略大小写可使用NOT ILIKE。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerNotEndWithCondition extends SqlServerEndWithCondition {

    /**
     * 单例实例
     */
    public final static SqlServerNotEndWithCondition INSTANCE = new SqlServerNotEndWithCondition();

    /**
     * 构造不以指定值结尾条件
     */
    public SqlServerNotEndWithCondition() {
        super(I18nHelper.notEndWith(), "NOT LIKE");
    }
}
