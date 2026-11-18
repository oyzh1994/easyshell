package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不以指定值结尾条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleNotEndWithCondition extends OracleEndWithCondition {

    /**
     * 单例实例
     */
    public final static OracleNotEndWithCondition INSTANCE = new OracleNotEndWithCondition();

    /**
     * 构造不以指定值结尾条件
     */
    public OracleNotEndWithCondition() {
        super(I18nHelper.notEndWith(), "NOT LIKE");
    }

}
