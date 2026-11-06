package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.common.util.HexUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBCondition;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * PostgreSQL数据库查询条件基类
 *
 * @author oyzh
 * @since 2025-11-06
 */
public abstract class PostgresqlCondition extends DBCondition {

    /**
     * 构造条件
     */
    public PostgresqlCondition() {
        super();
    }

    /**
     * 构造条件
     *
     * @param name  条件名称
     * @param value 条件值
     */
    public PostgresqlCondition(String name, String value) {
        super(name, value);
    }

    /**
     * 构造条件
     *
     * @param name             条件名称
     * @param value            条件值
     * @param requireCondition 是否需要输入条件值
     */
    public PostgresqlCondition(String name, String value, boolean requireCondition) {
        super(name, value, requireCondition);
    }

    /**
     * 包装条件值
     *
     * @param condition 条件值
     * @return 包装后的条件值
     */
    public String wrapCondition(Object condition) {
        return this.wrapValue(condition);
    }

    @Override
    public String wrapCondition(String columnName, Object condition) {
        if (this.isRequireCondition()) {
            return condition == null ? this.getValue() : this.getValue() + " " + this.wrapCondition(condition);
        }
        return this.wrapCondition(condition);
    }

    /**
     * 包装单个值，按PostgreSQL的字面量规则处理
     * 字符串使用标准单引号转义，布尔值使用TRUE/FALSE，二进制使用bytea的十六进制字面量
     *
     * @param condition 值
     * @return 包装后的值
     */
    protected String wrapValue(Object condition) {
        if (condition == null) {
            return null;
        }
        // 二进制数据，使用bytea的十六进制字面量
        if (condition instanceof byte[] bytes) {
            if (bytes.length == 0) {
                return "NULL";
            }
            return "E'\\\\x" + HexUtil.encodeHexStr(bytes, false) + "'";
        }
        // 布尔值，PostgreSQL使用TRUE/FALSE
        if (condition instanceof Boolean bool) {
            return bool ? "TRUE" : "FALSE";
        }
        // 数值
        if (condition instanceof Number) {
            return condition.toString();
        }
        // 字符串、日期时间等，由方言统一处理（PostgreSQL使用两个单引号转义）
        Object value = DBUtil.wrapData(condition, DBDialect.POSTGRESQL);
        return value == null ? null : value.toString();
    }

    /**
     * 包装列表值，按逗号拆分为多个PostgreSQL字面量
     * 例如：1,2,3 => IN (1, 2, 3)；a,b => IN ('a', 'b')
     *
     * @param condition 值
     * @return 包装后的值
     */
    protected String wrapListCondition(Object condition) {
        if (condition == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(this.getValue()).append(" (");
        String[] values = condition.toString().split(",");
        boolean first = true;
        for (String value : values) {
            value = value.trim();
            if (value.isEmpty()) {
                continue;
            }
            if (!first) {
                builder.append(", ");
            }
            builder.append(this.wrapListValue(value));
            first = false;
        }
        // 没有有效值时按空字符串处理，保证生成的SQL合法
        if (first) {
            builder.append("''");
        }
        return builder.append(")").toString();
    }

    /**
     * 包装列表中的单个值，数字等未知类型字面量由PostgreSQL自动转换为字段类型
     *
     * @param value 值
     * @return 包装后的值
     */
    private String wrapListValue(String value) {
        // 已经加引号的值
        if (value.length() > 1 && value.startsWith("'") && value.endsWith("'")) {
            return value;
        }
        // 字符串，使用标准单引号转义
        return "'" + value.replace("'", "''") + "'";
    }
}
