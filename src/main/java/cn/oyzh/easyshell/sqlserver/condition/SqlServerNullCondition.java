package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 为空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerNullCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerNullCondition INSTANCE = new SqlServerNullCondition();

    /**
     * 构造为空条件
     */
    public SqlServerNullCondition() {
        super(I18nHelper.isNull(), "IS NULL", false);
    }
}
