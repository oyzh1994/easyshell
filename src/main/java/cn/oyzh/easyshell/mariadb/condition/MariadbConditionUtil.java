package cn.oyzh.easyshell.mariadb.condition;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordFilter;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbNodeUtil;
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
 * @since 2026-10-09
 */
public class MariadbConditionUtil {

    /**
     * 初始化
     */
    public static void init() {
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbNotContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbNotEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbGtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbGtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbLtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbLtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbNotNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbNotEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbNotInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbNotBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbNotStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbEndWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MARIADB, MariadbNotEndWithCondition.INSTANCE);
    }

    /**
     * 构建条件
     *
     * @param filters 过滤条件
     * @return 条件
     * @throws Exception 异常
     */
    public static String buildCondition(List<MariadbRecordFilter> filters) throws Exception {
        if (filters == null || filters.isEmpty()) {
            return "";
        }
        StringBuilder conditions = new StringBuilder();
        for (int i = 0; i < filters.size(); i++) {
            MariadbRecordFilter filter = filters.get(i);
            String condition = filter.condition();
            if (StringUtil.isNotBlank(condition)) {
                conditions.append(DBUtil.wrap(filter.column(), DBDialect.MARIADB))
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
    public static boolean isInCondition(MariadbCondition condition) {
        return condition == MariadbInListCondition.INSTANCE || condition == MariadbNotInListCondition.INSTANCE;
    }

    /**
     * 是否介于条件
     *
     * @param condition 条件
     * @return 结果
     */
    public static boolean isBetweenCondition(MariadbCondition condition) {
        return condition == MariadbBetweenCondition.INSTANCE || condition == MariadbNotBetweenCondition.INSTANCE;
    }

    /**
     * 生成节点
     *
     * @param column    字段
     * @param condition 条件
     * @return 节点
     */
    public static List<Node> generateNode(MariadbColumn column, MariadbCondition condition) {
        condition = condition == null ? (MariadbCondition) DBConditionManager.conditions(DBDialect.MARIADB).getFirst() : condition;
        List<Node> list = new ArrayList<>();
        if (isInCondition(condition)) {
            ClearableTextField node = new ClearableTextField();
            node.setDisable(!condition.isRequireCondition());
            list.add(node);
        } else if (isBetweenCondition(condition)) {
            Node node1 = ShellMariadbNodeUtil.generateNode(column, false);
            Node node2 = ShellMariadbNodeUtil.generateNode(column, false);
            node1.setDisable(!condition.isRequireCondition());
            node2.setDisable(!condition.isRequireCondition());
            list.add(node1);
            list.add(node2);
        } else {
            Node node = ShellMariadbNodeUtil.generateNode(column, false);
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
                ShellMariadbNodeUtil.setNodeVal(controls.get(i), list.get(i));
            } else {
                ShellMariadbNodeUtil.setNodeVal(controls.get(i), value);
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
            return ShellMariadbNodeUtil.getNodeVal(controls.getFirst());
        }
        List<Object> list = new ArrayList<>();
        for (Node control : controls) {
            list.add(ShellMariadbNodeUtil.getNodeVal(control));
        }
        return list;
    }
}
