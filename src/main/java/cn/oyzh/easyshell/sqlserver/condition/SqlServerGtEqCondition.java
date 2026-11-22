package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.sqlserver.condition.SqlServerCondition;

/**
 * 大于等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerGtEqCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerGtEqCondition INSTANCE = new SqlServerGtEqCondition();

    /**
     * 构造大于等于条件
     */
    public SqlServerGtEqCondition() {
        super(I18nHelper.gtEq(), ">=");
    }
}
