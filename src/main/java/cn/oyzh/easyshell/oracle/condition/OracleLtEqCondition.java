package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleLtEqCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleLtEqCondition INSTANCE = new OracleLtEqCondition();

    /**
     * 构造小于等于条件
     */
    public OracleLtEqCondition() {
        super(I18nHelper.ltEq(), "<=");
    }
}
