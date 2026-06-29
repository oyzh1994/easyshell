package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisHashKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisListKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisSetKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisStreamKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisStringKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.key.ShellRedisZSetKeyTreeItem;
import cn.oyzh.easyshell.trees.redis.root.ShellRedisKeyRootTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;


/**
 * 树节点过滤器
 *
 * @author oyzh
 * @since 2023/06/30
 */
public class ShellRedisKeyTreeItemFilter implements RichTreeItemFilter {

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
     * 关键字
     */
    private String kw;

    /**
     * 匹配大小写
     */
    private boolean matchCase;

    /**
     * 全字匹配
     */
    private boolean wholeWord;

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

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

    //    /**
    //     * 0: 键
    //     * 1: 数据
    //     * 2: 键+数据
    //     */
    //    private byte scope;
    //
    //    public byte getScope() {
    //        return scope;
    //    }
    //
    //    public void setScope(byte scope) {
    //        this.scope = scope;
    //    }

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 根节点不参与过滤
        if (item instanceof ShellRedisKeyRootTreeItem) {
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
            // 关键字匹配
            //            if (StringUtil.isNotBlank(this.kw)) {
            //                // 匹配大小写
            //                boolean matchCase = this.matchMode == 1 || this.matchMode == 3;
            //                // 匹配全文
            //                boolean fullMatch = this.matchMode == 2 || this.matchMode == 3;
            // 键
            //                if (this.scope == 0 || this.scope == 2) {
            TextUtil.MatchText matchText = TextUtil.findText(key, this.kw, null, this.matchCase, this.wholeWord, false);
            return matchText != TextUtil.MatchText.NOT_FOUND;
            //                }
            //                // 数据
            //                if (this.scope == 1 || this.scope == 3) {
            //                    if (treeItem instanceof ShellRedisStringKeyTreeItem stringItem) {
            //                        Object data = stringItem.data();
            //                        String keyData = null;
            //                        if (data instanceof byte[] bytes) {
            //                            keyData = new String(bytes);
            //                        } else if (data instanceof String string) {
            //                            keyData = string;
            //                        }
            //                        if (keyData != null) {
            //                            return TextUtil.findIndex(keyData, this.kw, null, matchCase, fullMatch) != -1;
            //                        }
            //                    }
            //                }
            //            }
        }
        return true;
    }
}
