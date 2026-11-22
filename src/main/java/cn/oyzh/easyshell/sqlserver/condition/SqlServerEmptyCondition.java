package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 为空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerEmptyCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerEmptyCondition INSTANCE = new SqlServerEmptyCondition();

    /**
     * 构造为空条件
     */
    public SqlServerEmptyCondition() {
        super(I18nHelper.isEmpty(), "= ''", false);
    }
}
