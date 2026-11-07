package cn.oyzh.easyshell.trees.valkey;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyHashKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyListKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeySetKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyStreamKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyStringKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.key.ShellValkeyZSetKeyTreeItem;
import cn.oyzh.easyshell.trees.valkey.query.ShellValkeyQueryTreeItem;
import cn.oyzh.easyshell.trees.valkey.root.ShellValkeyRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;


/**
 * redis树节点过滤器
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyTreeItemFilter extends RichTreeItemFilter {

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

    /**
     * 获取类型
     *
     * @return 类型
     */
    public byte getType() {
        return type;
    }

    /**
     * 设置类型
     *
     * @param type 类型
     */
    public void setType(byte type) {
        this.type = type;
    }

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 根节点不参与过滤
        if (item instanceof ShellValkeyRootTreeItem) {
            return true;
        }
        // 键节点
        if (item instanceof ShellValkeyKeyTreeItem treeItem) {
            // 仅收藏
            if (1 == this.type && !treeItem.isCollect()) {
                return false;
            }
            // string
            if (2 == this.type && !(treeItem instanceof ShellValkeyStringKeyTreeItem)) {
                return false;
            }
            // list
            if (3 == this.type && !(treeItem instanceof ShellValkeyListKeyTreeItem)) {
                return false;
            }
            // set
            if (4 == this.type && !(treeItem instanceof ShellValkeySetKeyTreeItem)) {
                return false;
            }
            // zset
            if (5 == this.type && !(treeItem instanceof ShellValkeyZSetKeyTreeItem)) {
                return false;
            }
            // hash
            if (6 == this.type && !(treeItem instanceof ShellValkeyHashKeyTreeItem)) {
                return false;
            }
            // stream
            if (7 == this.type && !(treeItem instanceof ShellValkeyStreamKeyTreeItem)) {
                return false;
            }
            String key = treeItem.key();
            TextUtil.MatchText matchText = TextUtil.findText(key, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        // 查询节点
        if (item instanceof ShellValkeyQueryTreeItem queryItem) {
            String name = queryItem.queryName();
            TextUtil.MatchText matchText = TextUtil.findText(name, this.getKw(), null, this.isMatchCase(), this.isWholeWord(), false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
        }
        return true;
    }
}
