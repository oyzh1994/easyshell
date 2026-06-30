package cn.oyzh.easyshell.mongo.trees;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;

/**
 * 树节点过滤器
 *
 * @author oyzh
 * @since 2023/06/30
 */
public class MongoTreeItemFilter extends RichTreeItemFilter {

    /**
     * 仅看收藏键
     */
    private boolean onlyCollect;

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 不参与过滤的节点
        if (item != null && !item.isFilterable()) {
            return true;
        }
        return true;
    }

    public boolean isOnlyCollect() {
        return onlyCollect;
    }

    public void setOnlyCollect(boolean onlyCollect) {
        this.onlyCollect = onlyCollect;
    }
}
