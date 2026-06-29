package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisHashKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisListKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisSetKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisStreamKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisStringKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisZSetKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.root.ShellRedisRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;


/**
 * 树节点过滤器
 *
 * @author oyzh
 * @since 2023/06/30
 */
public class ShellRedisTreeItemFilter extends RichTreeItemFilter {

    /**
     * 0. 所有键
     * 1. 收藏键
     * 2. string
     * 3. list
     * 4. set
     * 5. zset
     * 6. hash
     * 7. stream
     */
    private byte type;

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 根节点不参与过滤
        if (item instanceof ShellRedisRootTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof ShellRedisKeyTreeItem treeItem) {
            // 仅收藏
            if (1 == this.type && !treeItem.isCollect()) {
                return false;
            }
            // string
            if (2 == this.type && !(treeItem instanceof ShellRedisStringKeyTreeItem)) {
                return false;
            }
            // list
            if (3 == this.type && !(treeItem instanceof ShellRedisListKeyTreeItem)) {
                return false;
            }
            // set
            if (4 == this.type && !(treeItem instanceof ShellRedisSetKeyTreeItem)) {
                return false;
            }
            // zset
            if (5 == this.type && !(treeItem instanceof ShellRedisZSetKeyTreeItem)) {
                return false;
            }
            // hash
            if (6 == this.type && !(treeItem instanceof ShellRedisHashKeyTreeItem)) {
                return false;
            }
            // stream
            if (7 == this.type && !(treeItem instanceof ShellRedisStreamKeyTreeItem)) {
                return false;
            }
            String key = treeItem.key();
            TextUtil.MatchText matchText = TextUtil.findText(key, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
