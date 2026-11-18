package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.oracle.condition.OracleCondition;

/**
 * 不为空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleNotEmptyCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleNotEmptyCondition INSTANCE = new OracleNotEmptyCondition();

    /**
     * 构造不为空条件
     */
    public OracleNotEmptyCondition() {
        super(I18nHelper.notIsEmpty(), "!=''", false);
    }
}
