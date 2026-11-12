package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collection;

/**
 * 介于条件
 * <p>
 * PostgreSQL使用BETWEEN ... AND ...，区间值按PostgreSQL字面量规则包装。
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlBetweenCondition extends PostgresqlCondition {

    /**
     * 单例实例
     */
    public final static PostgresqlBetweenCondition INSTANCE = new PostgresqlBetweenCondition();

    /**
     * 构造介于条件
     */
    public PostgresqlBetweenCondition() {
        super(I18nHelper.between(), "BETWEEN");
    }

    /**
     * 构造介于条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public PostgresqlBetweenCondition(String name, String value) {
        super(name, value);
    }

    @Override
    public String wrapCondition(Object condition) {
        if (condition instanceof Object[] arr) {
            return this.wrapCondition(arr[0]) + " AND " + this.wrapCondition(arr[1]);
        }
        if (condition instanceof Collection coll) {
            return this.wrapCondition(CollectionUtil.get(coll, 0)) + " AND " + this.wrapCondition(CollectionUtil.get(coll, 1));
        }
        return super.wrapCondition(condition);
    }
}
