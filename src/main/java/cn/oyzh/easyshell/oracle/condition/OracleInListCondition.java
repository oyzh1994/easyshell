package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 在列表条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleInListCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleInListCondition INSTANCE = new OracleInListCondition();

    /**
     * 构造在列表条件
     */
    public OracleInListCondition() {
        super(I18nHelper.inList(), "IN");
    }

    public OracleInListCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return this.getValue() + " " +super.wrapListCondition(condition);
    }
}
