package cn.oyzh.easyshell.trees.mysql;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mysql.event.MysqlEventsTreeItem;
import cn.oyzh.easyshell.trees.mysql.function.MysqlFunctionsTreeItem;
import cn.oyzh.easyshell.trees.mysql.procedure.MysqlProceduresTreeItem;
import cn.oyzh.easyshell.trees.mysql.root.MysqlRootTreeItem;
import cn.oyzh.easyshell.trees.mysql.table.MysqlTablesTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.MysqlViewsTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * mysql树节点过滤器
 *
 * @author oyzh
 * @since 2023/06/30
 */
public class MysqlTreeItemFilter implements RichTreeItemFilter {

    /**
     * 关键字
     */
    private String kw;

    /**
     * 0. 包含
     * 1. 包含 + 大小写符合
     * 2. 全字匹配
     * 3. 全字匹配 + 大小写符合
     */
    private byte matchMode;

    public String getKw() {
        return kw;
    }

    public void setKw(String kw) {
        this.kw = kw;
    }

    public byte getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(byte matchMode) {
        this.matchMode = matchMode;
    }

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 部分节点不参与过滤
        if (item instanceof MysqlRootTreeItem
                || item instanceof MysqlDatabaseTreeItem
                || item instanceof MysqlEventsTreeItem
                || item instanceof MysqlFunctionsTreeItem
                || item instanceof MysqlProceduresTreeItem
                || item instanceof MysqlTablesTreeItem
                || item instanceof MysqlViewsTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof MysqlTreeItem<?> treeItem) {
            RichTreeItemValue value = treeItem.getValue();
            String name = value.name();
            // 关键字匹配
            if (StringUtil.isNotBlank(this.kw)) {
                // 匹配大小写
                boolean matchCase = this.matchMode == 1 || this.matchMode == 3;
                // 匹配全文
                boolean fullMatch = this.matchMode == 2 || this.matchMode == 3;
                // 名称
                int index = TextUtil.findIndex(name, this.kw, null, matchCase, fullMatch);
                return index != -1;
            }
        }
        return true;
    }
}
