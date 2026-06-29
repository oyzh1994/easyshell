package cn.oyzh.easyshell.trees.mongo;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;

/**
 * 树节点过滤器
 *
 * @author oyzh
 * @since 2023/06/30
 */
public class MongoTreeItemFilter implements RichTreeItemFilter {

    /**
     * 仅看收藏键
     */
    private boolean onlyCollect;


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
