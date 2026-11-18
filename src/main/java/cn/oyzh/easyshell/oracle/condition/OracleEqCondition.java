package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleEqCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleEqCondition INSTANCE = new OracleEqCondition();

    /**
     * 构造等于条件
     */
    public OracleEqCondition() {
        super(I18nHelper.eq(), "=");
    }
}
