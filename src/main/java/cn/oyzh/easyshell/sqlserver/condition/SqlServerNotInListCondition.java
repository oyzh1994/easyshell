package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不在列表条件
 * <p>
 * SqlServer使用NOT IN (...)，列表值按逗号拆分，字符串使用标准单引号转义。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerNotInListCondition extends SqlServerInListCondition {

    /**
     * 单例实例
     */
    public final static SqlServerNotInListCondition INSTANCE = new SqlServerNotInListCondition();

    /**
     * 构造不在列表条件
     */
    public SqlServerNotInListCondition() {
        super(I18nHelper.notInList(), "NOT IN");
    }
}
