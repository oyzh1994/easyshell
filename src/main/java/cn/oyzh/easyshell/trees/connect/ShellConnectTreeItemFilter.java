package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;

/**
 * 连接节点过滤器
 *
 * @author oyzh
 * @since 2025/05/28
 */
public class ShellConnectTreeItemFilter extends RichTreeItemFilter {

    @Override
    public boolean test(RichTreeItem<?> item) {
        if (StringUtil.isNotBlank(this.getKw()) && item instanceof ShellConnectTreeItem treeItem) {
            return StringUtil.containsIgnoreCase(treeItem.connectName(), this.getKw());
        }
        return true;
    }
}
