package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collection;

/**
 * 不介于区间条件
 * <p>
 * PostgreSQL使用NOT BETWEEN ... AND ...，区间值按PostgreSQL字面量规则包装。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlNotBetweenCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlNotBetweenCondition INSTANCE = new PostgresqlNotBetweenCondition();

    /**
     * 构造不介于区间条件
     */
    public PostgresqlNotBetweenCondition() {
        super(I18nHelper.notBetween(), "NOT BETWEEN");
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition instanceof Object[] arr) {
            return this.getValue() + " " + this.wrapValue(arr[0]) + " AND " + this.wrapValue(arr[1]);
        }
        if (condition instanceof Collection coll) {
            return this.getValue() + " " + this.wrapValue(CollectionUtil.get(coll, 0)) + " AND " + this.wrapValue(CollectionUtil.get(coll, 1));
        }
        return super.wrapCondition(condition);
    }
}
