package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 介于条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlBetweenCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlBetweenCondition INSTANCE = new MysqlBetweenCondition();

    /**
     * 构造介于条件
     */
    public MysqlBetweenCondition() {
        super(I18nHelper.between(), "BETWEEN");
    }

    /**
     * 构造介于条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public MysqlBetweenCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        return super.wrapBetweenCondition(condition);
    }
}
