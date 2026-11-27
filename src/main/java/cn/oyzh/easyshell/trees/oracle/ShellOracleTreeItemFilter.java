package cn.oyzh.easyshell.trees.oracle;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.index.ShellIndexTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexesTreeItem;
import cn.oyzh.easyshell.trees.trigger.ShellTriggerTreeItem;
import cn.oyzh.easyshell.trees.trigger.ShellTriggersTreeItem;
import cn.oyzh.easyshell.trees.type.ShellTypeTreeItem;
import cn.oyzh.easyshell.trees.type.ShellTypesTreeItem;
import cn.oyzh.easyshell.trees.oracle.function.ShellOracleFunctionsTreeItem;
import cn.oyzh.easyshell.trees.oracle.procedure.ShellOracleProceduresTreeItem;
import cn.oyzh.easyshell.trees.oracle.query.ShellOracleQueriesTreeItem;
import cn.oyzh.easyshell.trees.oracle.root.ShellOracleRootTreeItem;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.easyshell.trees.oracle.table.ShellOracleTablesTreeItem;
import cn.oyzh.easyshell.trees.oracle.view.ShellOracleViewsTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * Oracle数据库树节点过滤器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleTreeItemFilter extends RichTreeItemFilter {

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 公共自定义类型节点
        if (item instanceof ShellTypesTreeItem) {
            return true;
        }
        if (item instanceof ShellTypeTreeItem typeItem) {
            String name = typeItem.getValue().name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
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
        if (item instanceof ShellOracleRootTreeItem
                || item instanceof ShellOracleViewsTreeItem
                || item instanceof ShellOracleTablesTreeItem
                || item instanceof ShellOracleQueriesTreeItem
                || item instanceof ShellOracleSchemaTreeItem
                || item instanceof ShellOracleFunctionsTreeItem
                || item instanceof ShellOracleProceduresTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof ShellOracleTreeItem<?> treeItem) {
            RichTreeItemValue value = treeItem.getValue();
            String name = value.name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
