package cn.oyzh.easyshell.trees.zk;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;

/**
 * 树节点过滤器
 *
 * @author oyzh
 * @since 2023/3/28
 */
public class ShellZKTreeItemFilter extends RichTreeItemFilter {

    /**
     * 0. 所有节点
     * 1. 收藏节点
     * 2. 持久节点
     * 3. 临时节点
     */
    private byte type;

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 根节点直接展示
        if (item instanceof ShellZKNodeTreeItem treeItem) {
            // 根节点不参与过滤
            if (treeItem.isRootNode()) {
                return true;
            }
            // 仅收藏
            if (1 == this.type && !treeItem.isCollect()) {
                return false;
            }
            // 仅持久节点
            if (2 == this.type && treeItem.isEphemeralNode()) {
                return false;
            }
            // 仅临时节点
            if (3 == this.type && !treeItem.isEphemeralNode()) {
                return false;
            }
            String nodePath = treeItem.decodeNodePath();
            TextUtil.MatchText matchText = TextUtil.findText(nodePath, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }
}
