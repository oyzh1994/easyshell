package cn.oyzh.easyshell.trees.mongo;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionsTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.function.ShellMongoFunctionsTreeItem;
import cn.oyzh.easyshell.trees.mongo.query.ShellMongoQueriesTreeItem;
import cn.oyzh.easyshell.trees.mongo.root.ShellMongoRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * 树节点过滤器
 *
 * @author oyzh
 * @since 2023/06/30
 */
public class ShellMongoTreeItemFilter extends RichTreeItemFilter {

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 不参与过滤的节点
        if (item != null && !item.isFilterable()) {
            return true;
        }
        // 部分节点不参与过滤
        if (item instanceof ShellMongoRootTreeItem
                || item instanceof ShellMongoCollectionsTreeItem
                || item instanceof ShellMongoQueriesTreeItem
                || item instanceof ShellMongoFunctionsTreeItem
                || item instanceof ShellMongoDatabaseTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof ShellMongoTreeItem<?> treeItem) {
            RichTreeItemValue value = treeItem.getValue();
            String name = value.name();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
