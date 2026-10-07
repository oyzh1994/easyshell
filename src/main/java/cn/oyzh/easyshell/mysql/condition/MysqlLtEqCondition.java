package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于等于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlLtEqCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlLtEqCondition INSTANCE = new MysqlLtEqCondition();

    /**
     * 构造小于等于条件
     */
    public MysqlLtEqCondition() {
        super(I18nHelper.ltEq(), "<=");
    }
}
