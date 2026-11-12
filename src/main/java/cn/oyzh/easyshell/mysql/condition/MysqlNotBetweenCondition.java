package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不介于区间条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlNotBetweenCondition extends MysqlBetweenCondition {

    /**
     * 单例实例
     */
    public final static MysqlNotBetweenCondition INSTANCE = new MysqlNotBetweenCondition();

    /**
     * 构造不介于区间条件
     */
    public MysqlNotBetweenCondition() {
        super(I18nHelper.notBetween(), "NOT BETWEEN");
    }
}
