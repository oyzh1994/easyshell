package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.oracle.condition.OracleCondition;

/**
 * 为空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleEmptyCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleEmptyCondition INSTANCE = new OracleEmptyCondition();

    /**
     * 构造为空条件
     */
    public OracleEmptyCondition() {
        super(I18nHelper.isEmpty(), "=''", false);
    }
}
