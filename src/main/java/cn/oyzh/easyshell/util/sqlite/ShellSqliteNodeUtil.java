package cn.oyzh.easyshell.util.sqlite;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.fx.db.util.DBNodeUtil;
import javafx.scene.Node;

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
}
