package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 介于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleBetweenCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleBetweenCondition INSTANCE = new OracleBetweenCondition();

    /**
     * 构造介于条件
     */
    public OracleBetweenCondition() {
        super(I18nHelper.between(), "BETWEEN");
    }

    public OracleBetweenCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return this.getValue() + " " + super.wrapBetweenCondition(condition);
    }
}
