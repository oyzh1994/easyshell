package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不等于条件
 * <p>
 * SqlServer使用&lt;&gt;表示不等于。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerNotEqCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerNotEqCondition INSTANCE = new SqlServerNotEqCondition();

    /**
     * 构造不等于条件
     */
    public SqlServerNotEqCondition() {
        super(I18nHelper.notEq(), "<>");
    }
}
