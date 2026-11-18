package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.oracle.condition.OracleCondition;

/**
 * 大于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleGtCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleGtCondition INSTANCE = new OracleGtCondition();

    /**
     * 构造大于条件
     */
    public OracleGtCondition() {
        super(I18nHelper.gt(), ">");
    }
}
