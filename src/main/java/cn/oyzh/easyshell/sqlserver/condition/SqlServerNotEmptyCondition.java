package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不为空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerNotEmptyCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerNotEmptyCondition INSTANCE = new SqlServerNotEmptyCondition();

    /**
     * 构造不为空条件
     */
    public SqlServerNotEmptyCondition() {
        super(I18nHelper.notIsEmpty(), "<> ''", false);
    }
}
