package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerLtCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerLtCondition INSTANCE = new SqlServerLtCondition();

    /**
     * 构造小于条件
     */
    public SqlServerLtCondition() {
        super(I18nHelper.lt(), "<");
    }
}
