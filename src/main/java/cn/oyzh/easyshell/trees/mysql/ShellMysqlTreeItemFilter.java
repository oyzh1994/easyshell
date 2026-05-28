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
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * mysql树节点过滤器
 *
 * @author oyzh
 * @since 2023/06/30
 */
public class ShellMysqlTreeItemFilter implements RichTreeItemFilter {

    /**
     * 关键字
     */
    private String kw;

    /**
     * 匹配大小写
     */
    private boolean matchCase;

    /**
     * 全字模式
     */
    private boolean wholeWord;

    public String getKw() {
        return kw;
    }

    public void setKw(String kw) {
        this.kw = kw;
    }


    public boolean isMatchCase() {
        return matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    public boolean isWholeWord() {
        return wholeWord;
    }

    public void setWholeWord(boolean wholeWord) {
        this.wholeWord = wholeWord;
    }

    @Override
    public boolean test(RichTreeItem<?> item) {
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
            TextUtil.MatchText matchText = TextUtil.findText(name, this.kw, null, this.matchCase, this.wholeWord, false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
