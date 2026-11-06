package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 为空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlNullCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlNullCondition INSTANCE = new PostgresqlNullCondition();

    /**
     * 构造为空条件
     */
    public PostgresqlNullCondition() {
        super(I18nHelper.isNull(), "IS NULL", false);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : condition.toString();
    }
}
