package cn.oyzh.easyshell.trees.mariadb;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.event.ShellMariadbEventsTreeItem;
import cn.oyzh.easyshell.trees.mariadb.function.ShellMariadbFunctionsTreeItem;
import cn.oyzh.easyshell.trees.mariadb.procedure.ShellMariadbProceduresTreeItem;
import cn.oyzh.easyshell.trees.mariadb.query.ShellMariadbQueriesTreeItem;
import cn.oyzh.easyshell.trees.mariadb.root.ShellMariadbRootTreeItem;
import cn.oyzh.easyshell.trees.mariadb.table.ShellMariadbTablesTreeItem;
import cn.oyzh.easyshell.trees.mariadb.view.ShellMariadbViewsTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * MariaDB树节点过滤器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTreeItemFilter extends RichTreeItemFilter {

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 部分节点不参与过滤
        if (item instanceof ShellMariadbRootTreeItem
                || item instanceof ShellMariadbViewsTreeItem
                || item instanceof ShellMariadbEventsTreeItem
                || item instanceof ShellMariadbTablesTreeItem
                || item instanceof ShellMariadbQueriesTreeItem
                || item instanceof ShellMariadbDatabaseTreeItem
                || item instanceof ShellMariadbFunctionsTreeItem
                || item instanceof ShellMariadbProceduresTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof ShellMariadbTreeItem<?> treeItem) {
            RichTreeItemValue value = treeItem.getValue();
            String name = value.name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
