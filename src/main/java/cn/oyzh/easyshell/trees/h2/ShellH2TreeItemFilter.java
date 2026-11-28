package cn.oyzh.easyshell.trees.h2;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.h2.query.ShellH2QueriesTreeItem;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
import cn.oyzh.easyshell.trees.h2.function.ShellH2RoutinesTreeItem;
import cn.oyzh.easyshell.trees.h2.sequence.ShellH2SequencesTreeItem;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TablesTreeItem;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewsTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexTreeItem;
import cn.oyzh.easyshell.trees.index.ShellIndexesTreeItem;
import cn.oyzh.easyshell.trees.trigger.ShellTriggerTreeItem;
import cn.oyzh.easyshell.trees.trigger.ShellTriggersTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * H2树节点过滤器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2TreeItemFilter extends RichTreeItemFilter {

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
        if (item instanceof ShellH2RootTreeItem
                || item instanceof ShellH2SchemaTreeItem
                || item instanceof ShellH2TablesTreeItem
                || item instanceof ShellH2ViewsTreeItem
                || item instanceof ShellH2RoutinesTreeItem
                || item instanceof ShellH2SequencesTreeItem
                || item instanceof ShellH2QueriesTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof ShellH2TreeItem<?> treeItem) {
            RichTreeItemValue value = treeItem.getValue();
            String name = value.name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
