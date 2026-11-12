package cn.oyzh.easyshell.sqlite.condition;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordFilter;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteNodeUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBConditionManager;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 查询条件工具。
 *
 * @since 2026-11-02
 */
public class SQLiteConditionUtil {

    public static final SQLiteCondition CONTAINS = SQLiteContainsCondition.INSTANCE;
    public static final SQLiteCondition NOT_CONTAINS = SQLiteNotContainsCondition.INSTANCE;
    public static final SQLiteCondition EQ = SQLiteEqCondition.INSTANCE;
    public static final SQLiteCondition NOT_EQ = SQLiteNotEqCondition.INSTANCE;
    public static final SQLiteCondition GT = SQLiteGtCondition.INSTANCE;
    public static final SQLiteCondition GT_EQ = SQLiteGtEqCondition.INSTANCE;
    public static final SQLiteCondition LT_EQ = SQLiteLtEqCondition.INSTANCE;
    public static final SQLiteCondition LT = SQLiteLtCondition.INSTANCE;
    public static final SQLiteCondition NULL = SQLiteNullCondition.INSTANCE;
    public static final SQLiteCondition NOT_NULL = SQLiteNotNullCondition.INSTANCE;
    public static final SQLiteCondition EMPTY = SQLiteEmptyCondition.INSTANCE;
    public static final SQLiteCondition NOT_EMPTY = SQLiteNotEmptyCondition.INSTANCE;
    public static final SQLiteCondition IN = SQLiteInListCondition.INSTANCE;
    public static final SQLiteCondition NOT_IN = SQLiteNotInListCondition.INSTANCE;
    public static final SQLiteCondition BETWEEN = SQLiteBetweenCondition.INSTANCE;
    public static final SQLiteCondition NOT_BETWEEN = SQLiteNotBetweenCondition.INSTANCE;
    public static final SQLiteCondition START_WITH = SQLiteStartWithCondition.INSTANCE;
    public static final SQLiteCondition NOT_START_WITH = SQLiteNotStartWithCondition.INSTANCE;
    public static final SQLiteCondition END_WITH = SQLiteEndWithCondition.INSTANCE;
    public static final SQLiteCondition NOT_END_WITH = SQLiteNotEndWithCondition.INSTANCE;

    private SQLiteConditionUtil() {
    }

    public static void init() {
        for (SQLiteCondition condition : List.of(CONTAINS, NOT_CONTAINS, EQ, NOT_EQ, GT, GT_EQ,
                LT_EQ, LT, NULL, NOT_NULL, EMPTY, NOT_EMPTY, IN, NOT_IN, BETWEEN, NOT_BETWEEN,
                START_WITH, NOT_START_WITH, END_WITH, NOT_END_WITH)) {
            DBConditionManager.putCondition(DBDialect.SQLITE, condition);
        }
    }

//    public static String buildCondition(List<SQLiteRecordFilter> filters) throws Exception {
//        if (filters == null || filters.isEmpty()) {
//            return "";
//        }
//        List<SQLiteRecordFilter> activeFilters = new ArrayList<>();
//        List<String> conditions = new ArrayList<>();
//        for (SQLiteRecordFilter filter : filters) {
//            String condition = filter.condition();
//            if (StringUtil.isNotBlank(condition)) {
//                activeFilters.add(filter);
//                conditions.add(SQLiteGeneratorUtil.quote(filter.column()) + " " + condition);
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

    public static boolean isInCondition(SQLiteCondition condition) {
        return condition == IN || condition == NOT_IN;
    }

    public static boolean isBetweenCondition(SQLiteCondition condition) {
        return condition == BETWEEN || condition == NOT_BETWEEN;
    }

    public static List<Node> generateNode(SQLiteColumn column, SQLiteCondition condition) {
        condition = condition == null ? CONTAINS : condition;
        List<Node> nodes = new ArrayList<>();
        if (isInCondition(condition)) {
            ClearableTextField node = new ClearableTextField();
            node.setDisable(!condition.isRequireCondition());
            nodes.add(node);
        } else if (isBetweenCondition(condition)) {
            Node first = ShellSqliteNodeUtil.generateNode(column, false);
            Node second = ShellSqliteNodeUtil.generateNode(column, false);
            first.setDisable(!condition.isRequireCondition());
            second.setDisable(!condition.isRequireCondition());
            nodes.add(first);
            nodes.add(second);
        } else {
            Node node = ShellSqliteNodeUtil.generateNode(column, false);
            node.setDisable(!condition.isRequireCondition());
            nodes.add(node);
        }
        return nodes;
    }

    public static void setNodeVal(List<Node> controls, Object value) {
        for (int index = 0; index < controls.size(); index++) {
            if (value instanceof List<?> list) {
                ShellSqliteNodeUtil.setNodeVal(controls.get(index), list.get(index));
            } else {
                ShellSqliteNodeUtil.setNodeVal(controls.get(index), value);
            }
        }
    }

    public static Object getNodeVal(List<Node> controls) throws Exception {
        if (controls == null || controls.isEmpty()) {
            return null;
        }
        if (controls.size() == 1) {
            return ShellSqliteNodeUtil.getNodeVal(controls.getFirst());
        }
        List<Object> values = new ArrayList<>();
        for (Node control : controls) {
            values.add(ShellSqliteNodeUtil.getNodeVal(control));
        }
        return values;
    }
}
