package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不等于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleNotEqCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleNotEqCondition INSTANCE = new OracleNotEqCondition();

    /**
     * 构造不等于条件
     */
    public OracleNotEqCondition() {
        super(I18nHelper.notEq(), "!=");
    }
}
