package cn.oyzh.easyshell.oracle.condition;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.record.OracleRecordFilter;
import cn.oyzh.easyshell.util.oracle.ShellOracleNodeUtil;
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
public class OracleConditionUtil {

    static {
        ensureRegistered();
    }

    /**
     * 确保条件初始化器已注册
     */
    public static void ensureRegistered() {
        DBConditionManager.registerInitializer(DBDialect.ORACLE, OracleConditionUtil::init);
    }

    /**
     * 初始化
     */
    public static void init() {
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleNotContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleGtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleLtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleNotEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleNotNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleNotEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleLtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleGtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleNotInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleNotBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleEndWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleNotStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.ORACLE, OracleNotEndWithCondition.INSTANCE);
    }

//    /**
//     * 构建条件
//     *
//     * @param filters 过滤条件
//     * @return 条件
//     * @throws Exception 异常
//     */
//    public static String buildCondition(List<OracleRecordFilter> filters) throws Exception {
//        if (filters == null || filters.isEmpty()) {
//            return "";
//        }
//        StringBuilder conditions = new StringBuilder();
//        for (int i = 0; i < filters.size(); i++) {
//            OracleRecordFilter filter = filters.get(i);
//            String condition = filter.condition();
//            if (StringUtil.isNotBlank(condition)) {
//                conditions.append(DBUtil.wrap(filter.column(), DBDialect.ORACLE))
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
    public static boolean isInCondition(OracleCondition condition) {
        return condition == OracleInListCondition.INSTANCE || condition == OracleNotInListCondition.INSTANCE;
    }

    /**
     * 是否介于条件
     *
     * @param condition 条件
     * @return 结果
     */
    public static boolean isBetweenCondition(OracleCondition condition) {
        return condition == OracleBetweenCondition.INSTANCE || condition == OracleNotBetweenCondition.INSTANCE;
    }

    /**
     * 生成节点
     *
     * @param column    字段
     * @param condition 条件
     * @return 节点
     */
    public static List<Node> generateNode(OracleColumn column, OracleCondition condition) {
        condition = condition == null ? (OracleCondition) DBConditionManager.conditions(DBDialect.ORACLE).getFirst() : condition;
        List<Node> list = new ArrayList<>();
        if (isInCondition(condition)) {
            ClearableTextField node = new ClearableTextField();
            node.setDisable(!condition.isRequireCondition());
            list.add(node);
        } else if (isBetweenCondition(condition)) {
            Node node1 = ShellOracleNodeUtil.generateNode(column, false);
            Node node2 = ShellOracleNodeUtil.generateNode(column, false);
            node1.setDisable(!condition.isRequireCondition());
            node2.setDisable(!condition.isRequireCondition());
            list.add(node1);
            list.add(node2);
        } else {
            Node node = ShellOracleNodeUtil.generateNode(column, false);
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
                ShellOracleNodeUtil.setNodeVal(controls.get(i), list.get(i));
            } else {
                ShellOracleNodeUtil.setNodeVal(controls.get(i), value);
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
            return ShellOracleNodeUtil.getNodeVal(controls.getFirst());
        }
        List<Object> list = new ArrayList<>();
        for (Node control : controls) {
            list.add(ShellOracleNodeUtil.getNodeVal(control));
        }
        return list;
    }
}
