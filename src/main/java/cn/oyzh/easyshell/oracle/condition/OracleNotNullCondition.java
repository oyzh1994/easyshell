package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 非空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleNotNullCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleNotNullCondition INSTANCE = new OracleNotNullCondition();

    /**
     * 构造非空条件
     */
    public OracleNotNullCondition() {
        super(I18nHelper.notIsNull(), "IS NOT NULL", false);
    }
}
