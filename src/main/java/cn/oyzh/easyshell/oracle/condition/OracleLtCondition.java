package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleLtCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleLtCondition INSTANCE = new OracleLtCondition();

    /**
     * 构造小于条件
     */
    public OracleLtCondition() {
        super(I18nHelper.lt(), "<");
    }
}
