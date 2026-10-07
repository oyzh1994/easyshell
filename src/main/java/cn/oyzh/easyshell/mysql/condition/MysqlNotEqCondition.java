package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不等于条件
 *
 * @author oyzh
 * @since 2024/6/27
 */
public class MysqlNotEqCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlNotEqCondition INSTANCE = new MysqlNotEqCondition();

    /**
     * 构造不等于条件
     */
    public MysqlNotEqCondition() {
        super(I18nHelper.notEq(), "!=");
    }
}
