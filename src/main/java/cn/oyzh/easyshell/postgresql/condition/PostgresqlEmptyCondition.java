package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 为空条件
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlEmptyCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlEmptyCondition INSTANCE = new PostgresqlEmptyCondition();

    /**
     * 构造为空条件
     */
    public PostgresqlEmptyCondition() {
        super(I18nHelper.isEmpty(), "= ''", false);
    }
}
