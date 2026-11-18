package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值开头条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleNotStartWithCondition extends OracleStartWithCondition {

    /**
     * 单例实例
     */
    public final static OracleNotStartWithCondition INSTANCE = new OracleNotStartWithCondition();

    /**
     * 构造不以指定值开头条件
     */
    public OracleNotStartWithCondition() {
        super(I18nHelper.notStartWith(), "NOT LIKE");
    }
}
