package cn.oyzh.easyshell.mysql.condition;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.db.DBDialect;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.util.mysql.ShellMysqlNodeUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * 条件工具类
 *
 * @author oyzh
 * @since 2024/6/26
 */
public class MysqlConditionUtil {

    /**
     * 获取条件
     *
     * @return 条件列表
     */
    public static List<MysqlCondition> conditions() {
        List<MysqlCondition> conditions = new ArrayList<>();
        conditions.add(MysqlContainsCondition.INSTANCE);
        conditions.add(MysqlNotContainsCondition.INSTANCE);
        conditions.add(MysqlEqCondition.INSTANCE);
        conditions.add(MysqlGtCondition.INSTANCE);
        conditions.add(MysqlLtCondition.INSTANCE);
        conditions.add(MysqlNotEqCondition.INSTANCE);
        conditions.add(MysqlNullCondition.INSTANCE);
        conditions.add(MysqlNotNullCondition.INSTANCE);
        conditions.add(MysqlEmptyCondition.INSTANCE);
        conditions.add(MysqlNotEmptyCondition.INSTANCE);
        conditions.add(MysqlLtEqCondition.INSTANCE);
        conditions.add(MysqlGtEqCondition.INSTANCE);
        conditions.add(MysqlInListCondition.INSTANCE);
        conditions.add(MysqlNotInListCondition.INSTANCE);
        conditions.add(MysqlBetweenCondition.INSTANCE);
        conditions.add(MysqlNotBetweenCondition.INSTANCE);
        conditions.add(MysqlStartWithCondition.INSTANCE);
        conditions.add(MysqlEndWithCondition.INSTANCE);
        conditions.add(MysqlNotStartWithCondition.INSTANCE);
        conditions.add(MysqlNotEndWithCondition.INSTANCE);
        return conditions;
    }

    /**
     * 构建条件
     *
     * @param filters 过滤条件
     * @return 条件
     */
    public static String buildCondition(List<MysqlRecordFilter> filters) throws Exception {
        if (filters == null || filters.isEmpty()) {
            return "";
        }
        StringBuilder conditions = new StringBuilder();
        for (int i = 0; i < filters.size(); i++) {
            MysqlRecordFilter filter = filters.get(i);
            String condition = filter.condition();
            if (StringUtil.isNotBlank(condition)) {
                conditions.append(ShellMysqlUtil.wrap(filter.column(), DBDialect.MYSQL))
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
    public static boolean isInCondition(MysqlCondition condition) {
        return condition == MysqlInListCondition.INSTANCE || condition == MysqlNotInListCondition.INSTANCE;
    }

    /**
     * 是否介于条件
     *
     * @param condition 条件
     * @return 结果
     */
    public static boolean isBetweenCondition(MysqlCondition condition) {
        return condition == MysqlBetweenCondition.INSTANCE || condition == MysqlNotBetweenCondition.INSTANCE;
    }

    /**
     * 生成节点
     *
     * @param column    字段
     * @param condition 条件
     * @return 节点
     */
    public static List<Node> generateNode(MysqlColumn column, MysqlCondition condition) {
        condition = condition == null ? conditions().getFirst() : condition;
        List<Node> list = new ArrayList<>();
        if (isInCondition(condition)) {
            ClearableTextField node = new ClearableTextField();
            node.setDisable(!condition.isRequireCondition());
            list.add(node);
        } else if (isBetweenCondition(condition)) {
            Node node1 = ShellMysqlNodeUtil.generateNode(column, false);
            Node node2 = ShellMysqlNodeUtil.generateNode(column, false);
            node1.setDisable(!condition.isRequireCondition());
            node2.setDisable(!condition.isRequireCondition());
            list.add(node1);
            list.add(node2);
        } else {
            Node node = ShellMysqlNodeUtil.generateNode(column, false);
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
                ShellMysqlNodeUtil.setNodeVal(controls.get(i), list.get(i));
            } else {
                ShellMysqlNodeUtil.setNodeVal(controls.get(i), value);
            }
        }
    }

    /**
     * 获取节点值
     *
     * @param controls 组件
     * @return 值
     */
    public static Object getNodeVal(List<Node> controls) throws Exception {
        if (controls == null || controls.isEmpty()) {
            return null;
        }
        if (controls.size() == 1) {
            return ShellMysqlNodeUtil.getNodeVal(controls.getFirst());
        }
        List<Object> list = new ArrayList<>();
        for (Node control : controls) {
            list.add(ShellMysqlNodeUtil.getNodeVal(control));
        }
        return list;
    }
}
