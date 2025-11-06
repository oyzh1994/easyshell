package cn.oyzh.easyshell.trees.mysql;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;

/**
 * 树节点过滤器
 *
 * @author oyzh
 * @since 2023/06/30
 */
public class DBTreeItemFilter implements RichTreeItemFilter {

    /**
     * 仅看收藏键
     */
    private boolean onlyCollect;

//    /**
//     * db主页搜索处理
//     */
//    private DBSearchHandler searchHandler;

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 不参与过滤的节点
        if (item != null && !item.isFilterable()) {
            return true;
        }
//        // 判断是否满足搜索要求
//        DBSearchParam param = this.searchHandler.searchParam();
//        if (param != null && !param.isEmpty() && param.isFilterMode()) {
//            return this.searchHandler.getMatchType(item) != null;
//        }
        return true;
    }


    public boolean isOnlyCollect() {
        return onlyCollect;
    }

    public void setOnlyCollect(boolean onlyCollect) {
        this.onlyCollect = onlyCollect;
    }
}
