package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 非空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerNotNullCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerNotNullCondition INSTANCE = new SqlServerNotNullCondition();

    /**
     * 构造非空条件
     */
    public SqlServerNotNullCondition() {
        super(I18nHelper.notIsNull(), "IS NOT NULL", false);
    }
}
