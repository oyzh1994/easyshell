package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 非空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlNotNullCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlNotNullCondition INSTANCE = new PostgresqlNotNullCondition();

    /**
     * 构造非空条件
     */
    public PostgresqlNotNullCondition() {
        super(I18nHelper.notIsNull(), "IS NOT NULL", false);
    }
}
