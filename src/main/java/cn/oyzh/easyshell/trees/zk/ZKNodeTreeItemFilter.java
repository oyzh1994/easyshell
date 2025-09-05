package cn.oyzh.easyshell.trees.zk;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ZKFilter;
import cn.oyzh.easyshell.store.zk.ZKFilterStore;
import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemFilter;

import java.util.List;

/**
 * 树节点过滤器
 *
 * @author oyzh
 * @since 2023/3/28
 */
public class ZKNodeTreeItemFilter implements RichTreeItemFilter {

    /**
     * 0. 所有节点
     * 1. 收藏节点
     * 2. 持久节点
     * 3. 临时节点
     */
    private byte type;

   /**
    * 0: 路径
    * 1: 数据
    * 2: 路径+数据
    */
   private byte scope;

    /**
     * 关键字
     */
    private String kw;

    /**
     * 0. 包含
     * 1. 包含+大小写符合
     * 2. 全字匹配
     * 3. 全字匹配+大小写符合
     */
    private byte matchMode;

    /**
     * 过滤内容列表
     */
    private List<ZKFilter> filters;

    /**
     * 过滤配置储存
     */
    private final ZKFilterStore filterStore = ZKFilterStore.INSTANCE;

    /**
     * 初始化过滤配置
     */
    public void initFilters(String iid) {
        this.filters = this.filterStore.loadEnable(iid);
    }

    @Override
    public boolean test(RichTreeItem<?> item) {
        // 根节点直接展示
        if (item instanceof ZKNodeTreeItem treeItem) {
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
            // 过滤节点
            if (ShellZKNodeUtil.isFiltered(nodePath, this.filters)) {
                return false;
            }
            // 关键字
            if (StringUtil.isNotBlank(this.kw)) {
//                // 路径
               if (this.scope == 0) {
                    if (this.matchMode == 0) {
                        return StringUtil.containsIgnoreCase(nodePath, this.kw);
                    }
                    if (this.matchMode == 1) {
                        return StringUtil.contains(nodePath, this.kw);
                    }
                    if (this.matchMode == 2) {
                        return StringUtil.equalsIgnoreCase(nodePath, this.kw);
                    }
                    if (this.matchMode == 3) {
                        return StringUtil.equals(nodePath, this.kw);
                    }
               } else if (this.scope == 1) {// 数据
                   byte[] bytes = treeItem.getData();
                   String nodeData = new String(bytes);
                   if (this.matchMode == 0) {
                       return StringUtil.containsIgnoreCase(nodeData, this.kw);
                   }
                   if (this.matchMode == 1) {
                       return StringUtil.contains(nodeData, this.kw);
                   }
                   if (this.matchMode == 2) {
                       return StringUtil.equalsIgnoreCase(nodeData, this.kw);
                   }
                   if (this.matchMode == 3) {
                       return StringUtil.equals(nodeData, this.kw);
                   }
               } else if (this.scope == 2) {// 路径+数据
                   byte[] bytes = treeItem.getData();
                   String nodeData = new String(bytes);
                   if (this.matchMode == 0) {
                       return StringUtil.containsIgnoreCase(nodePath, this.kw) || StringUtil.containsIgnoreCase(nodeData, this.kw);
                   }
                   if (this.matchMode == 1) {
                       return StringUtil.contains(nodePath, this.kw) || StringUtil.contains(nodeData, this.kw);
                   }
                   if (this.matchMode == 2) {
                       return StringUtil.equalsIgnoreCase(nodePath, this.kw) || StringUtil.equalsIgnoreCase(nodeData, this.kw);
                   }
                   if (this.matchMode == 3) {
                       return StringUtil.equals(nodePath, this.kw) || StringUtil.equals(nodeData, this.kw);
                   }
               }
            }
        }
        return true;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

   public byte getScope() {
       return scope;
   }

   public void setScope(byte scope) {
       this.scope = scope;
   }

    public String getKw() {
        return kw;
    }

    public void setKw(String kw) {
        this.kw = kw;
    }

    public byte getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(byte matchMode) {
        this.matchMode = matchMode;
    }

    public List<ZKFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<ZKFilter> filters) {
        this.filters = filters;
    }
}
