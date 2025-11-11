package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 结束以条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlEndWithCondition extends MysqlCondition {

    public final static MysqlEndWithCondition INSTANCE = new MysqlEndWithCondition();

    public MysqlEndWithCondition() {
        super(I18nHelper.endWith(), "LIKE");
    }

    public MysqlEndWithCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition);
        }
        return super.wrapCondition(condition);
    }
}
