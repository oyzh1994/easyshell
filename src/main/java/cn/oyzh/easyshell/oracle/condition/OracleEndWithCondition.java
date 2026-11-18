package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值结尾条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleEndWithCondition extends OracleCondition {

    /**
     * 单例实例
     */
    public final static OracleEndWithCondition INSTANCE = new OracleEndWithCondition();

    /**
     * 构造以指定值结尾条件
     */
    public OracleEndWithCondition() {
        super(I18nHelper.endWith(), "LIKE");
    }

    public OracleEndWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return this.getValue() + " " + super.wrapValue(condition + "%");
        }
        return null;
    }
}
