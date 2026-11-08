package cn.oyzh.easyshell.trees.http;

import cn.oyzh.easyshell.http.ShellHttpClient;
import cn.oyzh.easyshell.http.ShellHttpWorkspace;
import cn.oyzh.easyshell.tabs.http.ShellHttpTabPane;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * HTTP 工作区树节点基类。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public abstract class ShellHttpTreeItem<V extends RichTreeItemValue> extends RichTreeItem<V> {

    /**
     * 构造树节点
     *
     * @param treeView 树视图
     */
    public ShellHttpTreeItem(ShellHttpTreeView treeView) {
        super(treeView);
    }

    @Override
    public ShellHttpTreeView getTreeView() {
        return (ShellHttpTreeView) super.getTreeView();
    }

    /**
     * 获取节点数据
     *
     * @return 数据
     */
    public abstract Object data();

    /**
     * 获取工作区
     *
     * @return 工作区
     */
    public ShellHttpWorkspace workspace() {
        return this.getTreeView().workspace();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellHttpClient client() {
        return this.getTreeView().client();
    }

    /**
     * 获取标签页面板
     *
     * @return 标签页面板
     */
    public ShellHttpTabPane tabPane() {
        return this.getTreeView().getTabPane();
    }

    /**
     * 刷新树
     */
    protected void reloadTree() {
        this.getTreeView().reload();
    }
}
