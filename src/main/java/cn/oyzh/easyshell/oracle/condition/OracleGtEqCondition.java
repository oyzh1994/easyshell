package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.oracle.condition.OracleCondition;

/**
 * 大于等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleGtEqCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleGtEqCondition INSTANCE = new OracleGtEqCondition();

    /**
     * 构造大于等于条件
     */
    public OracleGtEqCondition() {
        super(I18nHelper.gtEq(), ">=");
    }
}
