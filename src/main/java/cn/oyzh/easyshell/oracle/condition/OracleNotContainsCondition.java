package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

import cn.oyzh.easyshell.oracle.condition.OracleCondition;

/**
 * 不包含条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleNotContainsCondition extends OracleContainsCondition {

    /**
     * 单例实例
     */
    public final static OracleNotContainsCondition INSTANCE = new OracleNotContainsCondition();

    /**
     * 构造不包含条件
     */
    public OracleNotContainsCondition() {
        super(I18nHelper.notContains(), "NOT LIKE");
    }
}
