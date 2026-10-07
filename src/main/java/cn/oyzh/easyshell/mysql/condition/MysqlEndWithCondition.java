package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 以指定值结尾条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlEndWithCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlEndWithCondition INSTANCE = new MysqlEndWithCondition();

    /**
     * 构造以指定值结尾条件
     */
    public MysqlEndWithCondition() {
        super(I18nHelper.endWith(), "LIKE");
    }

    /**
     * 构造以指定值结尾条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
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
