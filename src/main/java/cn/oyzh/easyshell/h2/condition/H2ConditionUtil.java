package cn.oyzh.easyshell.h2.condition;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.record.H2RecordFilter;
import cn.oyzh.easyshell.util.h2.ShellH2NodeUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBConditionManager;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 查询条件工具。
 *
 * @since 2026-11-02
 */
public class H2ConditionUtil {

    static {
        ensureRegistered();
    }

    /**
     * 确保条件初始化器已注册
     */
    public static void ensureRegistered() {
        DBConditionManager.registerInitializer(DBDialect.H2, H2ConditionUtil::init);
    }

    public static final H2Condition CONTAINS = H2ContainsCondition.INSTANCE;
    public static final H2Condition NOT_CONTAINS = H2NotContainsCondition.INSTANCE;
    public static final H2Condition EQ = H2EqCondition.INSTANCE;
    public static final H2Condition NOT_EQ = H2NotEqCondition.INSTANCE;
    public static final H2Condition GT = H2GtCondition.INSTANCE;
    public static final H2Condition GT_EQ = H2GtEqCondition.INSTANCE;
    public static final H2Condition LT_EQ = H2LtEqCondition.INSTANCE;
    public static final H2Condition LT = H2LtCondition.INSTANCE;
    public static final H2Condition NULL = H2NullCondition.INSTANCE;
    public static final H2Condition NOT_NULL = H2NotNullCondition.INSTANCE;
    public static final H2Condition EMPTY = H2EmptyCondition.INSTANCE;
    public static final H2Condition NOT_EMPTY = H2NotEmptyCondition.INSTANCE;
    public static final H2Condition IN = H2InListCondition.INSTANCE;
    public static final H2Condition NOT_IN = H2NotInListCondition.INSTANCE;
    public static final H2Condition BETWEEN = H2BetweenCondition.INSTANCE;
    public static final H2Condition NOT_BETWEEN = H2NotBetweenCondition.INSTANCE;
    public static final H2Condition START_WITH = H2StartWithCondition.INSTANCE;
    public static final H2Condition NOT_START_WITH = H2NotStartWithCondition.INSTANCE;
    public static final H2Condition END_WITH = H2EndWithCondition.INSTANCE;
    public static final H2Condition NOT_END_WITH = H2NotEndWithCondition.INSTANCE;

    private H2ConditionUtil() {
    }

    public static void init() {
        for (H2Condition condition : List.of(CONTAINS, NOT_CONTAINS, EQ, NOT_EQ, GT, GT_EQ,
                LT_EQ, LT, NULL, NOT_NULL, EMPTY, NOT_EMPTY, IN, NOT_IN, BETWEEN, NOT_BETWEEN,
                START_WITH, NOT_START_WITH, END_WITH, NOT_END_WITH)) {
            DBConditionManager.putCondition(DBDialect.H2, condition);
        }
    }

//    public static String buildCondition(List<H2RecordFilter> filters) throws Exception {
//        if (filters == null || filters.isEmpty()) {
//            return "";
//        }
//        List<H2RecordFilter> activeFilters = new ArrayList<>();
//        List<String> conditions = new ArrayList<>();
//        for (H2RecordFilter filter : filters) {
//            String condition = filter.condition();
//            if (StringUtil.isNotBlank(condition)) {
//                activeFilters.add(filter);
//                conditions.add(H2GeneratorUtil.quote(filter.column()) + " " + condition);
//            }
//        }
//        if (conditions.isEmpty()) {
//            return "";
//        }
//        StringBuilder builder = new StringBuilder();
//        for (int index = 0; index < conditions.size(); index++) {
//            builder.append(conditions.get(index));
//            if (index != conditions.size() - 1) {
//                builder.append(' ')
//                        .append(StringUtil.blankToDefault(activeFilters.get(index).getJoinSymbol(), "AND"))
//                        .append(' ');
//            }
//        }
//        return builder.toString();
//    }

    public static boolean isInCondition(H2Condition condition) {
        return condition == IN || condition == NOT_IN;
    }

    public static boolean isBetweenCondition(H2Condition condition) {
        return condition == BETWEEN || condition == NOT_BETWEEN;
    }

    public static List<Node> generateNode(H2Column column, H2Condition condition) {
        condition = condition == null ? CONTAINS : condition;
        List<Node> nodes = new ArrayList<>();
        if (isInCondition(condition)) {
            ClearableTextField node = new ClearableTextField();
            node.setDisable(!condition.isRequireCondition());
            nodes.add(node);
        } else if (isBetweenCondition(condition)) {
            Node first = ShellH2NodeUtil.generateNode(column, false);
            Node second = ShellH2NodeUtil.generateNode(column, false);
            first.setDisable(!condition.isRequireCondition());
            second.setDisable(!condition.isRequireCondition());
            nodes.add(first);
            nodes.add(second);
        } else {
            Node node = ShellH2NodeUtil.generateNode(column, false);
            node.setDisable(!condition.isRequireCondition());
            nodes.add(node);
        }
        return nodes;
    }

    public static void setNodeVal(List<Node> controls, Object value) {
        for (int index = 0; index < controls.size(); index++) {
            if (value instanceof List<?> list) {
                ShellH2NodeUtil.setNodeVal(controls.get(index), list.get(index));
            } else {
                ShellH2NodeUtil.setNodeVal(controls.get(index), value);
            }
        }
    }

    public static Object getNodeVal(List<Node> controls) throws Exception {
        if (controls == null || controls.isEmpty()) {
            return null;
        }
        if (controls.size() == 1) {
            return ShellH2NodeUtil.getNodeVal(controls.getFirst());
        }
        List<Object> values = new ArrayList<>();
        for (Node control : controls) {
            values.add(ShellH2NodeUtil.getNodeVal(control));
        }
        return values;
    }
}
