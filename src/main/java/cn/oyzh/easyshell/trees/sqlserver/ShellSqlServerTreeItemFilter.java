package cn.oyzh.easyshell.trees.sqlserver;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.sqlserver.function.ShellSqlServerFunctionsTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.procedure.ShellSqlServerProceduresTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.query.ShellSqlServerQueriesTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.root.ShellSqlServerRootTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.table.ShellSqlServerTablesTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.view.ShellSqlServerViewsTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexesTreeItem;
import cn.oyzh.easyshell.trees.trigger.ShellTriggerTreeItem;
import cn.oyzh.easyshell.trees.trigger.ShellTriggersTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * SqlServer数据库树节点过滤器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerTreeItemFilter extends RichTreeItemFilter {

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 公共触发器节点
        if (item instanceof ShellTriggersTreeItem) {
            return true;
        }
        if (item instanceof ShellTriggerTreeItem triggerItem) {
            String name = triggerItem.getValue().name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
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
        if (item instanceof ShellSqlServerRootTreeItem
                || item instanceof ShellSqlServerViewsTreeItem
                || item instanceof ShellSqlServerTablesTreeItem
                || item instanceof ShellSqlServerQueriesTreeItem
                || item instanceof ShellSqlServerSchemaTreeItem
                || item instanceof ShellSqlServerFunctionsTreeItem
                || item instanceof ShellSqlServerProceduresTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof ShellSqlServerTreeItem<?> treeItem) {
            RichTreeItemValue value = treeItem.getValue();
            String name = value.name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
