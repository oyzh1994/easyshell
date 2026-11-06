package cn.oyzh.easyshell.postgresql.condition;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordFilter;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlNodeUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBConditionManager;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * 条件工具类
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlConditionUtil {

    /**
     * 初始化
     */
    public static void init() {
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlNotContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlGtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlLtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlNotEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlNotNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlNotEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlLtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlGtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlNotInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlNotBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlEndWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlNotStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.POSTGRESQL, PostgresqlNotEndWithCondition.INSTANCE);
    }

    /**
     * 构建条件
     *
     * @param filters 过滤条件
     * @return 条件
     * @throws Exception 异常
     */
    public static String buildCondition(List<PostgresqlRecordFilter> filters) throws Exception {
        if (filters == null || filters.isEmpty()) {
            return "";
        }
        StringBuilder conditions = new StringBuilder();
        for (int i = 0; i < filters.size(); i++) {
            PostgresqlRecordFilter filter = filters.get(i);
            String condition = filter.condition();
            if (StringUtil.isNotBlank(condition)) {
                conditions.append(DBUtil.wrap(filter.column(), DBDialect.POSTGRESQL))
                        .append(" ")
                        .append(condition)
                        .append(" ");
            }
            if (i != filters.size() - 1) {
                conditions.append(filter.getJoinSymbol()).append(" ");
            }
        }
        return conditions.toString();
    }

    /**
     * 是否in条件
     *
     * @param condition 条件
     * @return 结果
     */
    public static boolean isInCondition(PostgresqlCondition condition) {
        return condition == PostgresqlInListCondition.INSTANCE || condition == PostgresqlNotInListCondition.INSTANCE;
    }

    /**
     * 是否介于条件
     *
     * @param condition 条件
     * @return 结果
     */
    public static boolean isBetweenCondition(PostgresqlCondition condition) {
        return condition == PostgresqlBetweenCondition.INSTANCE || condition == PostgresqlNotBetweenCondition.INSTANCE;
    }

    /**
     * 生成节点
     *
     * @param column    字段
     * @param condition 条件
     * @return 节点
     */
    public static List<Node> generateNode(PostgresqlColumn column, PostgresqlCondition condition) {
        condition = condition == null ? (PostgresqlCondition) DBConditionManager.conditions(DBDialect.POSTGRESQL).getFirst() : condition;
        List<Node> list = new ArrayList<>();
        if (isInCondition(condition)) {
            ClearableTextField node = new ClearableTextField();
            node.setDisable(!condition.isRequireCondition());
            list.add(node);
        } else if (isBetweenCondition(condition)) {
            Node node1 = ShellPostgresqlNodeUtil.generateNode(column, false);
            Node node2 = ShellPostgresqlNodeUtil.generateNode(column, false);
            node1.setDisable(!condition.isRequireCondition());
            node2.setDisable(!condition.isRequireCondition());
            list.add(node1);
            list.add(node2);
        } else {
            Node node = ShellPostgresqlNodeUtil.generateNode(column, false);
            node.setDisable(!condition.isRequireCondition());
            list.add(node);
        }
        return list;
    }

    /**
     * 设置节点值
     *
     * @param controls 组件
     * @param value    值
     */
    public static void setNodeVal(List<Node> controls, Object value) {
        for (int i = 0; i < controls.size(); i++) {
            if (value instanceof List<?> list) {
                ShellPostgresqlNodeUtil.setNodeVal(controls.get(i), list.get(i));
            } else {
                ShellPostgresqlNodeUtil.setNodeVal(controls.get(i), value);
            }
        }
    }

    /**
     * 获取节点值
     *
     * @param controls 组件
     * @return 值
     * @throws Exception 异常
     */
    public static Object getNodeVal(List<Node> controls) throws Exception {
        if (controls == null || controls.isEmpty()) {
            return null;
        }
        if (controls.size() == 1) {
            return ShellPostgresqlNodeUtil.getNodeVal(controls.getFirst());
        }
        List<Object> list = new ArrayList<>();
        for (Node control : controls) {
            list.add(ShellPostgresqlNodeUtil.getNodeVal(control));
        }
        return list;
    }
}
