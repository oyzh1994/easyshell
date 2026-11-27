package cn.oyzh.easyshell.trees.dameng;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.dameng.function.ShellDamengFunctionsTreeItem;
import cn.oyzh.easyshell.trees.dameng.procedure.ShellDamengProceduresTreeItem;
import cn.oyzh.easyshell.trees.dameng.query.ShellDamengQueriesTreeItem;
import cn.oyzh.easyshell.trees.dameng.root.ShellDamengRootTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.ShellDamengTablesTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewsTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexesTreeItem;
import cn.oyzh.easyshell.trees.trigger.ShellTriggerTreeItem;
import cn.oyzh.easyshell.trees.trigger.ShellTriggersTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * 达梦数据库树节点过滤器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengTreeItemFilter extends RichTreeItemFilter {

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
        if (item instanceof ShellDamengRootTreeItem
                || item instanceof ShellDamengViewsTreeItem
                || item instanceof ShellDamengTablesTreeItem
                || item instanceof ShellDamengQueriesTreeItem
                || item instanceof ShellDamengSchemaTreeItem
                || item instanceof ShellDamengFunctionsTreeItem
                || item instanceof ShellDamengProceduresTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof ShellDamengTreeItem<?> treeItem) {
            RichTreeItemValue value = treeItem.getValue();
            String name = value.name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
