package cn.oyzh.easyshell.util.sqlite;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.fx.db.util.DBNodeUtil;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 字段节点工具。
 *
 * @since 2026-11-02
 */
public final class ShellSqliteNodeUtil {

    private ShellSqliteNodeUtil() {
    }

    public static Object getNodeVal(Node node) {
        return DBNodeUtil.getNodeVal(node);
    }

    public static void setNodeVal(Node node, Object value) {
        DBNodeUtil.setNodeVal(node, value);
    }

    public static Node generateNode(SQLiteColumn column) {
        return generateNode(column, true);
    }

    public static Node generateNode(SQLiteColumn column, boolean handlerDefaultValue) {
        Node node = DBNodeUtil.generateNode(column);
        if (handlerDefaultValue && column != null && column.getDefaultValue() != null) {
            DBNodeUtil.setNodeVal(node, column.getDefaultValue());
        }
        return node;
    }

    /**
     * 生成字段标签
     *
     * @param column 字段
     * @return 标签列表
     */
    public static List<FXLabel> generateTags(SQLiteColumn column) {
        List<FXLabel> labels = new ArrayList<>();
        if (column.isNullable()) {
            FXLabel label = new FXLabel(I18nHelper.nullable());
            label.addClass("tag_nullable");
            labels.add(label);
        }
        if (column.isAutoIncrement()) {
            FXLabel label = new FXLabel(I18nHelper.autoIncrement());
            label.addClass("tag_autoIncrement");
            labels.add(label);
        }
        if (column.isPrimaryKey()) {
            FXLabel label = new FXLabel(I18nHelper.primaryKey());
            label.addClass("tag_primaryKey");
            labels.add(label);
        }
        if (column.isUnique()) {
            FXLabel label = new FXLabel(I18nResourceBundle.i18nString("shell.sqlite.unique"));
            label.addClass("tag_primaryKey");
            labels.add(label);
        }
        if (column.isHidden()) {
            FXLabel label = new FXLabel(I18nResourceBundle.i18nString("base.hidden"));
            label.addClass("tag_unsigned");
            labels.add(label);
        }
        if (column.isGenerated()) {
            FXLabel label = new FXLabel(I18nResourceBundle.i18nString("shell.sqlite.function.kind.generatedColumn"));
            label.addClass("tag_generated");
            labels.add(label);
        }
        return labels;
    }
}
