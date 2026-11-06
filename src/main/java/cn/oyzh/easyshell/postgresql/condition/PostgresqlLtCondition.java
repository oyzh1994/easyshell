package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 小于条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlLtCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlLtCondition INSTANCE = new PostgresqlLtCondition();

    /**
     * 构造小于条件
     */
    public PostgresqlLtCondition() {
        super(I18nHelper.lt(), "<");
    }
}
