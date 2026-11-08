package cn.oyzh.easyshell.trees.http;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.http.environment.ShellHttpEnvironmentTreeItem;
import cn.oyzh.easyshell.trees.http.request.ShellHttpRequestTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;

/**
 * HTTP 工作区树过滤器。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpTreeItemFilter extends RichTreeItemFilter {

    @Override
    public boolean test(RichTreeItem<?> item) {
        String keyword = this.getKw();
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        // 分类节点始终显示，只过滤请求和环境
        if (!(item instanceof ShellHttpRequestTreeItem)
                && !(item instanceof ShellHttpEnvironmentTreeItem)) {
            return true;
        }
        String text = item.getValue() == null ? null : item.getValue().text();
        if (text == null) {
            return false;
        }
        TextUtil.MatchText matchText = TextUtil.findText(text, keyword, null,
                this.isMatchCase(), this.isWholeWord(), false);
        return matchText != TextUtil.MatchText.NOT_FOUND;
    }
}
