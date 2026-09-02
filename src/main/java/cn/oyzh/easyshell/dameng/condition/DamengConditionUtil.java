package cn.oyzh.easyshell.dameng.condition;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.condition.DamengBetweenCondition;
import cn.oyzh.easyshell.dameng.condition.DamengCondition;
import cn.oyzh.easyshell.dameng.condition.DamengContainsCondition;
import cn.oyzh.easyshell.dameng.condition.DamengEndWithCondition;
import cn.oyzh.easyshell.dameng.condition.DamengEqCondition;
import cn.oyzh.easyshell.dameng.condition.DamengGtEqCondition;
import cn.oyzh.easyshell.dameng.condition.DamengInListCondition;
import cn.oyzh.easyshell.dameng.condition.DamengLtCondition;
import cn.oyzh.easyshell.dameng.condition.DamengLtEqCondition;
import cn.oyzh.easyshell.dameng.condition.DamengNotEndWithCondition;
import cn.oyzh.easyshell.dameng.condition.DamengNotEqCondition;
import cn.oyzh.easyshell.dameng.condition.DamengNotInListCondition;
import cn.oyzh.easyshell.dameng.condition.DamengNotNullCondition;
import cn.oyzh.easyshell.dameng.condition.DamengNotStartWithCondition;
import cn.oyzh.easyshell.dameng.condition.DamengNullCondition;
import cn.oyzh.easyshell.dameng.condition.DamengStartWithCondition;
import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.util.dameng.DamengNodeUtil;
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
 * @since 2024/6/26
 */
public class DamengConditionUtil {

    /**
     * 初始化
     */
    public static void init() {
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengNotContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengGtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengLtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengNotEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengNotNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengNotEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengLtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengGtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengNotInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengNotBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengEndWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengNotStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.DAMENG, DamengNotEndWithCondition.INSTANCE);
    }

    /**
     * 构建条件
     *
     * @param filters 过滤条件
     * @return 条件
     */
    public static String buildCondition(List<DamengRecordFilter> filters) throws Exception {
        if (filters == null || filters.isEmpty()) {
            return "";
        }
        StringBuilder conditions = new StringBuilder();
        for (int i = 0; i < filters.size(); i++) {
            DamengRecordFilter filter = filters.get(i);
            String condition = filter.condition();
            if (StringUtil.isNotBlank(condition)) {
                conditions.append(DBUtil.wrap(filter.column(), DBDialect.DAMENG))
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
    public static boolean isInCondition(DamengCondition condition) {
        return condition == DamengInListCondition.INSTANCE || condition == DamengNotInListCondition.INSTANCE;
    }

    /**
     * 是否介于条件
     *
     * @param condition 条件
     * @return 结果
     */
    public static boolean isBetweenCondition(DamengCondition condition) {
        return condition == DamengBetweenCondition.INSTANCE || condition == DamengNotBetweenCondition.INSTANCE;
    }

    /**
     * 生成节点
     *
     * @param column    字段
     * @param condition 条件
     * @return 节点
     */
    public static List<Node> generateNode(DamengColumn column, DamengCondition condition) {
        condition = condition == null ? (DamengCondition) DBConditionManager.conditions(DBDialect.DAMENG).getFirst() : condition;
        List<Node> list = new ArrayList<>();
        if (isInCondition(condition)) {
            ClearableTextField node = new ClearableTextField();
            node.setDisable(!condition.isRequireCondition());
            list.add(node);
        } else if (isBetweenCondition(condition)) {
            Node node1 = DamengNodeUtil.generateNode(column, false);
            Node node2 = DamengNodeUtil.generateNode(column, false);
            node1.setDisable(!condition.isRequireCondition());
            node2.setDisable(!condition.isRequireCondition());
            list.add(node1);
            list.add(node2);
        } else {
            Node node = DamengNodeUtil.generateNode(column, false);
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
                DamengNodeUtil.setNodeVal(controls.get(i), list.get(i));
            } else {
                DamengNodeUtil.setNodeVal(controls.get(i), value);
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
            return DamengNodeUtil.getNodeVal(controls.getFirst());
        }
        List<Object> list = new ArrayList<>();
        for (Node control : controls) {
            list.add(DamengNodeUtil.getNodeVal(control));
        }
        return list;
    }
}
