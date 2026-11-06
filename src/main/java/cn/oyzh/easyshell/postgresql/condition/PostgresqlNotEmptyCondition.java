package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不为空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlNotEmptyCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlNotEmptyCondition INSTANCE = new PostgresqlNotEmptyCondition();

    /**
     * 构造不为空条件
     */
    public PostgresqlNotEmptyCondition() {
        super(I18nHelper.notIsEmpty(), "<> ''", false);
    }

    @Override
    public String wrapCondition(Object condition) {
        return condition == null ? null : condition.toString();
    }
}
