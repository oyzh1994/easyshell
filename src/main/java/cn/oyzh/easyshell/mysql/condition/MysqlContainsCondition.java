package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 包含条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlContainsCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlContainsCondition INSTANCE = new MysqlContainsCondition();

    /**
     * 构造包含条件
     */
    public MysqlContainsCondition() {
        super(I18nHelper.contains(), "LIKE");
    }

    /**
     * 构造包含条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
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
