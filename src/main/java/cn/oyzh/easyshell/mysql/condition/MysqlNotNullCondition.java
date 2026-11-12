package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 非空条件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlNotNullCondition extends MysqlCondition {

    /**
     * 单例实例
     */
    public final static MysqlNotNullCondition INSTANCE = new MysqlNotNullCondition();

    /**
     * 构造非空条件
     */
    public MysqlNotNullCondition() {
        super(I18nHelper.notIsNull(), "IS NOT NULL", false);
    }
}
