package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不介于区间条件
 * <p>
 * SqlServer使用NOT BETWEEN ... AND ...，区间值按SqlServer字面量规则包装。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerNotBetweenCondition extends SqlServerBetweenCondition {

    /**
     * 单例实例
     */
    public final static SqlServerNotBetweenCondition INSTANCE = new SqlServerNotBetweenCondition();

    /**
     * 构造不介于区间条件
     */
    public SqlServerNotBetweenCondition() {
        super(I18nHelper.notBetween(), "NOT BETWEEN");
    }
}
