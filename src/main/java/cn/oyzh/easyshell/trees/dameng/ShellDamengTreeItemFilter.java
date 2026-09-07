package cn.oyzh.easyshell.trees.dameng;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.dameng.function.ShellDamengFunctionsTreeItem;
import cn.oyzh.easyshell.trees.dameng.procedure.ShellDamengProceduresTreeItem;
import cn.oyzh.easyshell.trees.dameng.query.ShellDamengQueriesTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.trees.dameng.table.ShellDamengTablesTreeItem;
import cn.oyzh.easyshell.trees.dameng.view.ShellDamengViewsTreeItem;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.event.ShellMysqlEventsTreeItem;
import cn.oyzh.easyshell.trees.mysql.function.ShellMysqlFunctionsTreeItem;
import cn.oyzh.easyshell.trees.mysql.procedure.ShellMysqlProceduresTreeItem;
import cn.oyzh.easyshell.trees.mysql.query.ShellMysqlQueriesTreeItem;
import cn.oyzh.easyshell.trees.mysql.root.ShellMysqlRootTreeItem;
import cn.oyzh.easyshell.trees.mysql.table.ShellMysqlTablesTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewsTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * 树节点过滤器
 *
 * @author oyzh
 * @since 2023/06/30
 */
public class ShellDamengTreeItemFilter extends RichTreeItemFilter {

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 部分节点不参与过滤
        if (item instanceof ShellMysqlRootTreeItem
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
