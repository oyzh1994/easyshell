package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerEqCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerEqCondition INSTANCE = new SqlServerEqCondition();

    /**
     * 构造等于条件
     */
    public SqlServerEqCondition() {
        super(I18nHelper.eq(), "=");
    }
}
