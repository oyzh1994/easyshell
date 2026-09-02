package cn.oyzh.easyshell.mongo.condition;

import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.record.MongoRecordFilter;
import cn.oyzh.easyshell.util.mongo.ShellMongoNodeUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.condition.DBConditionManager;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import com.mongodb.client.model.Filters;
import javafx.scene.Node;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 条件工具类
 *
 * @author oyzh
 * @since 2024/6/26
 */
public class MongoConditionUtil {

    /**
     * 初始化
     *
     */
    public static void init() {
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoNotContainsCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoNotEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoGtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoGtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoLtEqCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoLtCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoNotNullCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoNotEmptyCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoNotInListCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoNotBetweenCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoNotStartWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoEndWithCondition.INSTANCE);
        DBConditionManager.putCondition(DBDialect.MONGODB, MongoNotEndWithCondition.INSTANCE);
    }

    /**
     * 构建条件
     *
     * @param filters 过滤条件
     * @return 条件
     */
    public static Bson buildCondition(List<MongoRecordFilter> filters) {
        Bson bson = new Document();
        if (filters == null || filters.isEmpty()) {
            return bson;
        }
        boolean first = true;
        MongoRecordFilter lastFilter = null;
        for (MongoRecordFilter filter : filters) {
            if (!filter.isEnabled()) {
                continue;
            }
            Bson bson1 = filter.condition();
            if (bson1 != null) {
                if (first) {
                    bson = bson1;
                    first = false;
                } else {
                    if ("and".equalsIgnoreCase(lastFilter.getJoinSymbol())) {
                        bson = Filters.and(bson, bson1);
                    } else {
                        bson = Filters.or(bson, bson1);
                    }
                }
                lastFilter = filter;
            }
        }
        return bson;
    }

    /**
     * 是否in条件
     *
     * @param condition 条件
     * @return 结果
     */
    public static boolean isInCondition(MongoCondition condition) {
        return condition == MongoInListCondition.INSTANCE || condition == MongoNotInListCondition.INSTANCE;
    }

    /**
     * 是否介于条件
     *
     * @param condition 条件
     * @return 结果
     */
    public static boolean isBetweenCondition(MongoCondition condition) {
        return condition == MongoBetweenCondition.INSTANCE || condition == MongoNotBetweenCondition.INSTANCE;
    }

    /**
     * 生成节点
     *
     * @param column    字段
     * @param condition 条件
     * @return 节点
     */
    public static List<Node> generateNode(MongoColumn column, MongoCondition condition) {
        condition = condition == null ? (MongoCondition) DBConditionManager.conditions(DBDialect.MONGODB).getFirst() : condition;
        List<Node> list = new ArrayList<>();
        if (isInCondition(condition)) {
            ClearableTextField node = new ClearableTextField();
            node.setDisable(!condition.isRequireCondition());
            list.add(node);
        } else if (isBetweenCondition(condition)) {
            Node node1 = ShellMongoNodeUtil.generateNode(column);
            Node node2 = ShellMongoNodeUtil.generateNode(column);
            node1.setDisable(!condition.isRequireCondition());
            node2.setDisable(!condition.isRequireCondition());
            list.add(node1);
            list.add(node2);
        } else {
            Node node = ShellMongoNodeUtil.generateNode(column);
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
                ShellMongoNodeUtil.setNodeVal(controls.get(i), list.get(i));
            } else {
                ShellMongoNodeUtil.setNodeVal(controls.get(i), value);
            }
        }
    }

    /**
     * 获取节点值
     *
     * @param controls 组件
     * @return 值
     */
    public static Object getNodeVal(List<Node> controls) {
        if (controls == null || controls.isEmpty()) {
            return null;
        }
        if (controls.size() == 1) {
            return ShellMongoNodeUtil.getNodeVal(controls.getFirst());
        }
        List<Object> list = new ArrayList<>();
        for (Node control : controls) {
            list.add(ShellMongoNodeUtil.getNodeVal(control));
        }
        return list;
    }

    public static Bson idFilterRegex(Pattern pattern) {
        return Filters.expr(
                new Document("$regexMatch",
                        new Document("input", new Document("$toString", "$_id"))
                                .append("regex", pattern.pattern())
                )
        );
    }

    public static Bson idFilterRegexNot(Pattern pattern) {
        return Filters.expr(
                new Document("$not",
                        new Document("$regexMatch",
                                new Document("input", new Document("$toString", "$_id"))
                                        .append("regex", pattern.pattern())
                        )
                )
        );
    }
}
