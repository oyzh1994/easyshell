package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.i18n.I18nHelper;

/**
 * 不在列表条件
 * <p>
 * PostgreSQL使用NOT IN (...)，列表值按逗号拆分，字符串使用标准单引号转义。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlNotInListCondition extends PostgresqlInListCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlNotInListCondition INSTANCE = new PostgresqlNotInListCondition();

    /**
     * 构造不在列表条件
     */
    public PostgresqlNotInListCondition() {
        super(I18nHelper.notInList(), "NOT IN");
    }
}
