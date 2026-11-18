package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleContainsCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleContainsCondition INSTANCE = new OracleContainsCondition();

    /**
     * 构造包含条件
     */
    public OracleContainsCondition() {
        super(I18nHelper.contains(), "LIKE");
    }

    public OracleContainsCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return this.getValue() + " " + super.wrapValue("%" + condition + "%");
        }
        return null;
    }
}
