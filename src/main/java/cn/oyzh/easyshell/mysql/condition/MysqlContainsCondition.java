package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlContainsCondition extends MysqlCondition {

    public final static MysqlContainsCondition INSTANCE = new MysqlContainsCondition();

    public MysqlContainsCondition() {
        super(I18nHelper.contains(), "LIKE");
    }

    public MysqlContainsCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition != null) {
            return super.wrapCondition("%" + condition + "%");
        }
        return super.wrapCondition(condition);
    }
}
