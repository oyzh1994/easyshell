package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值开头条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleStartWithCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleStartWithCondition INSTANCE = new OracleStartWithCondition();

    /**
     * 构造以指定值开头条件
     */
    public OracleStartWithCondition() {
        super(I18nHelper.startWith(), "LIKE");
    }

    public OracleStartWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return this.getValue() + " " + super.wrapValue("%" + condition);
        }
        return null;
    }
}
