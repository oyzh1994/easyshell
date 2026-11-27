package cn.oyzh.easyshell.trees.mysql;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.event.ShellMysqlEventsTreeItem;
import cn.oyzh.easyshell.trees.mysql.function.ShellMysqlFunctionsTreeItem;
import cn.oyzh.easyshell.trees.mysql.procedure.ShellMysqlProceduresTreeItem;
import cn.oyzh.easyshell.trees.mysql.query.ShellMysqlQueriesTreeItem;
import cn.oyzh.easyshell.trees.mysql.root.ShellMysqlRootTreeItem;
import cn.oyzh.easyshell.trees.mysql.table.ShellMysqlTablesTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewsTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexesTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * mysql树节点过滤器
 *
 * @author oyzh
 * @since 2025-11-07
 */
public class ShellMysqlTreeItemFilter extends RichTreeItemFilter {

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 公共索引节点
        if (item instanceof ShellIndexesTreeItem) {
            return true;
        }
        if (item instanceof ShellIndexTreeItem indexItem) {
            String name = indexItem.getValue().name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        // 部分节点不参与过滤
        if (item instanceof ShellMysqlRootTreeItem
                || item instanceof ShellMysqlViewsTreeItem
                || item instanceof ShellMysqlEventsTreeItem
                || item instanceof ShellMysqlTablesTreeItem
                || item instanceof ShellMysqlQueriesTreeItem
                || item instanceof ShellMysqlDatabaseTreeItem
                || item instanceof ShellMysqlFunctionsTreeItem
                || item instanceof ShellMysqlProceduresTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof ShellMysqlTreeItem<?> treeItem) {
            RichTreeItemValue value = treeItem.getValue();
            String name = value.name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
