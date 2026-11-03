package cn.oyzh.easyshell.trees.sqlite;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.sqlite.function.ShellSqliteFunctionsTreeItem;
import cn.oyzh.easyshell.trees.sqlite.query.ShellSqliteQueriesTreeItem;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTablesTreeItem;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewsTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * SQLite树节点过滤器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTreeItemFilter extends RichTreeItemFilter {

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 部分节点不参与过滤
        if (item instanceof ShellSqliteRootTreeItem
                || item instanceof ShellSqliteTablesTreeItem
                || item instanceof ShellSqliteViewsTreeItem
                || item instanceof ShellSqliteFunctionsTreeItem
                || item instanceof ShellSqliteQueriesTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof ShellSqliteTreeItem<?> treeItem) {
            RichTreeItemValue value = treeItem.getValue();
            String name = value.name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
