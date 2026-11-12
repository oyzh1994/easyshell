package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 在列表条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlInListCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlInListCondition INSTANCE = new MysqlInListCondition();

    /**
     * 构造在列表条件
     */
    public MysqlInListCondition() {
        super(I18nHelper.inList(), "IN");
    }

    /**
     * 构造在列表条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public MysqlInListCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return this.getValue() + " " + super.wrapListCondition(condition);
    }
}
