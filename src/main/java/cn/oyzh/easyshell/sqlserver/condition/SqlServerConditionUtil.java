package cn.oyzh.easyshell.sqlserver.condition;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordFilter;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerNodeUtil;
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
public class SqlServerConditionUtil {

    static {
        ensureRegistered();
    }

    /**
     * 确保条件初始化器已注册
     */
    public static void ensureRegistered() {
        DBConditionManager.registerInitializer(DBDialect.SQLSERVER, SqlServerConditionUtil::init);
    }

    /**
     * 初始化
     */
    public static void init() {
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerNotContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerGtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerLtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerNotEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerNotNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerNotEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerLtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerGtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerNotInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerNotBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerEndWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerNotStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.SQLSERVER, SqlServerNotEndWithCondition.INSTANCE);
    }

//    /**
//     * 构建条件
//     *
//     * @param filters 过滤条件
//     * @return 条件
//     * @throws Exception 异常
//     */
//    public static String buildCondition(List<SqlServerRecordFilter> filters) throws Exception {
//        if (filters == null || filters.isEmpty()) {
//            return "";
//        }
//        StringBuilder conditions = new StringBuilder();
//        for (int i = 0; i < filters.size(); i++) {
//            SqlServerRecordFilter filter = filters.get(i);
//            String condition = filter.condition();
//            if (StringUtil.isNotBlank(condition)) {
//                conditions.append(DBUtil.wrap(filter.column(), DBDialect.SQLSERVER))
//                        .append(" ")
//                        .append(condition)
//                        .append(" ");
//            }
//            if (i != filters.size() - 1) {
//                conditions.append(filter.getJoinSymbol()).append(" ");
//            }
//        }
//        return conditions.toString();
//    }

    /**
     * 是否in条件
     *
     * @param condition 条件
     * @return 结果
     */
    public static boolean isInCondition(SqlServerCondition condition) {
        return condition == SqlServerInListCondition.INSTANCE || condition == SqlServerNotInListCondition.INSTANCE;
    }

    /**
     * 是否介于条件
     *
     * @param condition 条件
     * @return 结果
     */
    public static boolean isBetweenCondition(SqlServerCondition condition) {
        return condition == SqlServerBetweenCondition.INSTANCE || condition == SqlServerNotBetweenCondition.INSTANCE;
    }

    /**
     * 生成节点
     *
     * @param column    字段
     * @param condition 条件
     * @return 节点
     */
    public static List<Node> generateNode(SqlServerColumn column, SqlServerCondition condition) {
        condition = condition == null ? (SqlServerCondition) DBConditionManager.conditions(DBDialect.SQLSERVER).getFirst() : condition;
        List<Node> list = new ArrayList<>();
        if (isInCondition(condition)) {
            ClearableTextField node = new ClearableTextField();
            node.setDisable(!condition.isRequireCondition());
            list.add(node);
        } else if (isBetweenCondition(condition)) {
            Node node1 = ShellSqlServerNodeUtil.generateNode(column, false);
            Node node2 = ShellSqlServerNodeUtil.generateNode(column, false);
            node1.setDisable(!condition.isRequireCondition());
            node2.setDisable(!condition.isRequireCondition());
            list.add(node1);
            list.add(node2);
        } else {
            Node node = ShellSqlServerNodeUtil.generateNode(column, false);
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
                ShellSqlServerNodeUtil.setNodeVal(controls.get(i), list.get(i));
            } else {
                ShellSqlServerNodeUtil.setNodeVal(controls.get(i), value);
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
            return ShellSqlServerNodeUtil.getNodeVal(controls.getFirst());
        }
        List<Object> list = new ArrayList<>();
        for (Node control : controls) {
            list.add(ShellSqlServerNodeUtil.getNodeVal(control));
        }
        return list;
    }
}
