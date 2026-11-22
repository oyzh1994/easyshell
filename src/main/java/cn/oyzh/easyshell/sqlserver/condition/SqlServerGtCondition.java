package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.sqlserver.condition.SqlServerCondition;

/**
 * 大于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerGtCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerGtCondition INSTANCE = new SqlServerGtCondition();

    /**
     * 构造大于条件
     */
    public SqlServerGtCondition() {
        super(I18nHelper.gt(), ">");
    }
}
