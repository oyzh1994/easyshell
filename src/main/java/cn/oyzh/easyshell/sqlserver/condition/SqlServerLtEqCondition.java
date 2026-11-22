package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerLtEqCondition extends SqlServerCondition {

    /**
     * 单例实例
     */
    public final static SqlServerLtEqCondition INSTANCE = new SqlServerLtEqCondition();

    /**
     * 构造小于等于条件
     */
    public SqlServerLtEqCondition() {
        super(I18nHelper.ltEq(), "<=");
    }
}
