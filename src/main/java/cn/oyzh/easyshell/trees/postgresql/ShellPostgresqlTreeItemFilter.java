package cn.oyzh.easyshell.trees.postgresql;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.postgresql.function.ShellPostgresqlFunctionsTreeItem;
import cn.oyzh.easyshell.trees.postgresql.procedure.ShellPostgresqlProceduresTreeItem;
import cn.oyzh.easyshell.trees.postgresql.query.ShellPostgresqlQueriesTreeItem;
import cn.oyzh.easyshell.trees.postgresql.root.ShellPostgresqlRootTreeItem;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.trees.postgresql.table.ShellPostgresqlTablesTreeItem;
import cn.oyzh.easyshell.trees.postgresql.view.ShellPostgresqlViewsTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * PostgreSQL数据库树节点过滤器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlTreeItemFilter extends RichTreeItemFilter {

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 部分节点不参与过滤
        if (item instanceof ShellPostgresqlRootTreeItem
                || item instanceof ShellPostgresqlViewsTreeItem
                || item instanceof ShellPostgresqlTablesTreeItem
                || item instanceof ShellPostgresqlQueriesTreeItem
                || item instanceof ShellPostgresqlSchemaTreeItem
                || item instanceof ShellPostgresqlFunctionsTreeItem
                || item instanceof ShellPostgresqlProceduresTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof ShellPostgresqlTreeItem<?> treeItem) {
            RichTreeItemValue value = treeItem.getValue();
            String name = value.name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
